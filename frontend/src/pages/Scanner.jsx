import { Html5Qrcode, Html5QrcodeSupportedFormats } from 'html5-qrcode'
import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api, { payload, friendlyError } from '../api'
import { ErrorBanner } from '../components/Ui'

const BARCODE_FORMATS = [
  Html5QrcodeSupportedFormats.EAN_13,
  Html5QrcodeSupportedFormats.EAN_8,
  Html5QrcodeSupportedFormats.UPC_A,
  Html5QrcodeSupportedFormats.UPC_E,
  Html5QrcodeSupportedFormats.CODE_128,
  Html5QrcodeSupportedFormats.CODE_39,
  Html5QrcodeSupportedFormats.QR_CODE,
]

function cleanBarcode(value) {
  return String(value || '').replace(/\s+/g, '')
}

export default function Scanner() {
  const [manual, setManual] = useState('')
  const [error, setError] = useState('')
  const [info, setInfo] = useState('')
  const [loading, setLoading] = useState(false)
  const [scanning, setScanning] = useState(false)
  const scannerRef = useRef(null)
  const lookingUp = useRef(false)
  const navigate = useNavigate()

  async function lookup(raw, method) {
    const barcode = cleanBarcode(raw)
    if (!barcode) {
      setError('Enter the digits printed under the barcode.')
      return
    }
    if (lookingUp.current) return
    lookingUp.current = true
    setError('')
    setInfo('')
    setLoading(true)
    try {
      const res = await api.get(`/api/products/barcode/${encodeURIComponent(barcode)}`)
      const product = payload(res)
      if (!product?.id) {
        setError('That barcode did not match a pack we can show.')
        return
      }
      navigate(`/products/${product.id}`, { state: { scanMethod: method } })
    } catch (err) {
      setError(friendlyError(err, 'lookup'))
    } finally {
      lookingUp.current = false
      setLoading(false)
    }
  }

  async function stopCamera() {
    const html5 = scannerRef.current
    scannerRef.current = null
    if (html5) {
      try {
        await html5.stop()
      } catch {
        /* already stopped */
      }
      try {
        await html5.clear()
      } catch {
        /* ignore */
      }
    }
    setScanning(false)
  }

  async function startCamera() {
    setError('')
    setInfo('')
    await stopCamera()
    setScanning(true)
    try {
      const html5 = new Html5Qrcode('reader')
      scannerRef.current = html5
      const config = {
        fps: 10,
        qrbox: { width: 280, height: 160 },
        formatsToSupport: BARCODE_FORMATS,
      }
      const onDetect = (text) => {
        stopCamera().then(() => lookup(text, 'BARCODE'))
      }
      try {
        const cameras = await Html5Qrcode.getCameras()
        const rear = cameras.find((c) => /back|rear|environment/i.test(c.label))
        const cameraId = rear?.id || cameras[0]?.id
        if (!cameraId) {
          throw new Error('No camera found')
        }
        await html5.start(cameraId, config, onDetect)
      } catch {
        await html5.start({ facingMode: 'environment' }, config, onDetect)
      }
    } catch (err) {
      setError(
        err?.message?.includes('Permission') || err?.name === 'NotAllowedError'
          ? 'Camera permission was blocked. Allow the camera, or type the barcode instead.'
          : 'Camera did not open. Type the barcode instead.',
      )
      setScanning(false)
    }
  }

  useEffect(
    () => () => {
      scannerRef.current?.stop().catch(() => {})
    },
    [],
  )

  async function onFile(e) {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return
    setError('')
    setInfo('')
    setLoading(true)
    try {
      await stopCamera()
      const decoder = new Html5Qrcode('reader', { formatsToSupport: BARCODE_FORMATS, verbose: false })
      try {
        const text = await decoder.scanFile(file, false)
        await decoder.clear()
        await lookup(text, 'IMAGE')
        return
      } catch {
        try {
          await decoder.clear()
        } catch {
          /* ignore */
        }
      }
      const form = new FormData()
      form.append('file', file)
      const res = await api.post('/api/products/scan-image', form)
      const helper = payload(res)
      setInfo(
        helper?.message
          || 'Photo received. Type the digits printed under the barcode to open the pack.',
      )
    } catch {
      setError('Could not read a barcode from that photo. Type the digits under the barcode instead.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="scanner">
      <h1>Scan</h1>
      <p className="lede">Point at the barcode, type the digits, or upload a pack photo.</p>
      <ErrorBanner error={error} />
      {info && <p className="notice">{info}</p>}
      {loading && <p className="status">Looking up barcode…</p>}
      <div id="reader" className="reader" />
      {!scanning ? (
        <button type="button" className="btn-scan" onClick={startCamera}>
          Open camera
        </button>
      ) : (
        <button type="button" className="secondary" onClick={stopCamera}>
          Stop camera
        </button>
      )}
      <form
        className="search-row"
        onSubmit={(e) => {
          e.preventDefault()
          lookup(manual, 'BARCODE')
        }}
      >
        <label className="sr-only" htmlFor="barcode">
          Barcode
        </label>
        <input
          id="barcode"
          value={manual}
          onChange={(e) => setManual(e.target.value)}
          placeholder="Type barcode e.g. 8901058851010"
          required
        />
        <button type="submit">Look up</button>
      </form>
      <label className="file notice">
        Pack photo — we read the barcode from the picture
        <input type="file" accept="image/*" onChange={onFile} />
      </label>
    </div>
  )
}
