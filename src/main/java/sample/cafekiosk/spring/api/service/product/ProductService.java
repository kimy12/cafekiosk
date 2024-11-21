package sample.cafekiosk.spring.api.service.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sample.cafekiosk.spring.api.controller.product.dto.request.ProductCreateRequest;
import sample.cafekiosk.spring.api.service.product.response.ProductResponse;
import sample.cafekiosk.spring.domain.Product;
import sample.cafekiosk.spring.domain.ProductRepository;
import sample.cafekiosk.spring.domain.ProductSellingStatus;
import sample.cafekiosk.spring.domain.ProductType;

import java.util.List;
import java.util.stream.Collectors;

import static sample.cafekiosk.spring.domain.ProductSellingStatus.SELLING;
import static sample.cafekiosk.spring.domain.ProductType.HANDMADE;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductCreateRequest request) {
        // productNumber
        // 001 002 003 004
        // db에서 마지막 저장된 product의 상품번호를 읽어와서 +1
        // 009 -> 010
        String nextProductNumber = createNextProductNumber();


        return ProductResponse.builder()
                .productNumber(nextProductNumber)
                .type(request.getType())
                .sellingStatus(request.getSellingStatus())
                .name(request.getName())
                .price(request.getPrice())
                .build();
    }

    private String createNextProductNumber(){
        String latestProductNumber = productRepository.findLatestProductNumber();

        if(latestProductNumber == null){
            return "001";
        }

        int latestProductNumberInt = Integer.parseInt(latestProductNumber);
        int nextProductNumberInt = latestProductNumberInt + 1;

        return String.format("%03d", nextProductNumberInt);
    }

    /**
     * 화면에서 보이는 판매 상태
     * @return
     */
    public List<ProductResponse> getSellingProducts() {
        List<Product> products = productRepository.findAllBySellingStatusIn(ProductSellingStatus.forDisplay());

        return products.stream()
                .map(ProductResponse::of)
                .collect(Collectors.toList());
    }


}
