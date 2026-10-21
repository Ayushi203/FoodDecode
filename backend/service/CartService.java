package com.fooddecode.service;

import com.fooddecode.dto.cart.AddCartItemRequest;
import com.fooddecode.dto.cart.CartAnalysisResponse;
import com.fooddecode.dto.cart.CartResponse;
import com.fooddecode.entity.Cart;
import com.fooddecode.entity.CartItem;
import com.fooddecode.entity.Product;
import com.fooddecode.entity.ProductIngredient;
import com.fooddecode.entity.User;
import com.fooddecode.exception.ApiException;
import com.fooddecode.mapper.ProductMapper;
import com.fooddecode.repository.CartItemRepository;
import com.fooddecode.repository.CartRepository;
import com.fooddecode.repository.ProductIngredientRepository;
import com.fooddecode.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CurrentUserService currentUserService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductIngredientRepository productIngredientRepository;

    @Transactional
    public CartResponse addItem(AddCartItemRequest request) {
        if (request.getProductId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "productId is required");
        }
        int qty = request.getQuantity() == null ? 1 : request.getQuantity();
        if (qty < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
        }
        User user = currentUserService.requireUser();
        Cart cart = requireCart(user);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        CartItem item = cartItemRepository.findByCartAndProduct(cart, product).orElseGet(() -> {
            CartItem created = new CartItem();
            created.setCart(cart);
            created.setProduct(product);
            created.setQuantity(0);
            return created;
        });
        item.setQuantity(item.getQuantity() + qty);
        cartItemRepository.save(item);
        return getCart();
    }

    @Transactional
    public CartResponse setQuantity(Long itemId, int quantity) {
        if (quantity < 1) {
            return removeItem(itemId);
        }
        CartItem item = requireMine(itemId);
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return getCart();
    }

    @Transactional
    public CartResponse removeItem(Long itemId) {
        CartItem item = requireMine(itemId);
        cartItemRepository.delete(item);
        return getCart();
    }

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        User user = currentUserService.requireUser();
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) {
            return CartResponse.builder().cartId(null).items(List.of()).build();
        }
        List<CartResponse.CartItemResponse> items = cartItemRepository.findByCartOrderByIdAsc(cart).stream()
                .map(row -> CartResponse.CartItemResponse.builder()
                        .id(row.getId())
                        .quantity(row.getQuantity())
                        .product(ProductMapper.toSummary(row.getProduct()))
                        .build())
                .toList();
        return CartResponse.builder().cartId(cart.getId()).items(items).build();
    }

    @Transactional(readOnly = true)
    public CartAnalysisResponse analysis() {
        User user = currentUserService.requireUser();
        Cart cart = cartRepository.findByUser(user).orElse(null);
        List<CartItem> items = cart == null ? List.of() : cartItemRepository.findByCartOrderByIdAsc(cart);
        if (items.isEmpty()) {
            return CartAnalysisResponse.builder()
                    .itemCount(0)
                    .totalUnits(0)
                    .averageHealthScore(null)
                    .harmfulFlagCount(0)
                    .verdict("Your cart is empty. Add a pack to see a health read.")
                    .build();
        }
        int units = 0;
        int scoreSum = 0;
        int scored = 0;
        int harmful = 0;
        int processed = 0;
        for (CartItem item : items) {
            units += item.getQuantity();
            Product product = item.getProduct();
            if (product.getHealthScore() != null) {
                scoreSum += product.getHealthScore() * item.getQuantity();
                scored += item.getQuantity();
            }
            List<ProductIngredient> links =
                    productIngredientRepository.findByProductIdOrderByPositionInListAsc(product.getId());
            long flags = links.stream().filter(pi -> pi.getIngredient().isHarmful()).count();
            harmful += (int) flags * item.getQuantity();
            boolean sugaryOrTrans = links.stream().anyMatch(pi -> {
                String cat = pi.getIngredient().getHarmCategory();
                return cat != null && (cat.equalsIgnoreCase("trans_fat")
                        || cat.equalsIgnoreCase("added_sugar")
                        || cat.equalsIgnoreCase("artificial_sweetener"));
            });
            if (sugaryOrTrans || (product.getHealthScore() != null && product.getHealthScore() < 50)) {
                processed += item.getQuantity();
            }
        }
        Double avg = scored == 0 ? null : Math.round((scoreSum / (double) scored) * 10) / 10.0;
        String verdict;
        if (avg != null && avg >= 75 && processed == 0) {
            verdict = "This cart looks relatively clean — average score " + avg + "/100.";
        } else if (processed > 0) {
            verdict = "This cart leans processed — " + processed + " of " + units
                    + " units have added sugar, trans fat, sweeteners, or a poor health score.";
        } else {
            verdict = "Mixed cart. Average score " + (avg == null ? "—" : avg) + "/100, with "
                    + harmful + " harmful-ingredient flags across items.";
        }
        return CartAnalysisResponse.builder()
                .itemCount(items.size())
                .totalUnits(units)
                .averageHealthScore(avg)
                .harmfulFlagCount(harmful)
                .verdict(verdict)
                .build();
    }

    private Cart requireCart(User user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    private CartItem requireMine(Long itemId) {
        User user = currentUserService.requireUser();
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cart item not found"));
        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not your cart item");
        }
        return item;
    }
}
