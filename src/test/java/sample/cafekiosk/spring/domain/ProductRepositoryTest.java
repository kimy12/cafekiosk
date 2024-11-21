package sample.cafekiosk.spring.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static sample.cafekiosk.spring.domain.ProductSellingStatus.*;
import static sample.cafekiosk.spring.domain.ProductType.HANDMADE;

/**
 * @DataJpaTest
 *
 * 하나의 테스트가 끝나고 트랜잭션 롤백을 하기 때문에 따로 클랜징을 할 필요가 없다.
 *
 */
@ActiveProfiles("test") // test profilefh 로 설정값들이 돌거임
//@SpringBootTest
@DataJpaTest // SpringBootTest 보다 갸벼움 jpa관련된 bean 들만 주입
class ProductRepositoryTest {
    // repository 테스트는 단위테스트와 같은 성격이다.

    @Autowired
    private ProductRepository productRepository;

    @DisplayName("원하는 판매상태를 가진 상품들을 조회한다.")
    @Test
    void findAllBySellingStatusIn(){
        //given
        Product product1 = createProduct("001", HANDMADE, SELLING, "아메리카노", 4000);
        Product product2 = createProduct("002", HANDMADE, HOLD, "카페라떼", 4500);
        Product product3 = createProduct("003", HANDMADE, STOP_SELLING, "팥빙수", 7000);
        productRepository.saveAll(List.of(product1,product2,product3));

        //when
        List<Product> products = productRepository.findAllBySellingStatusIn(List.of(SELLING,HOLD));

        //then
        assertThat(products).hasSize(2)
                .extracting("productNumber","name","sellingStatus")
                .containsExactlyInAnyOrder(
                        tuple("001","아메리카노",SELLING),
                        tuple("002","카페라떼",HOLD)
                );
    }

    @DisplayName("가장 마지막으로 저장한 상품의 상품번호를 읽어온다.")
    @Test
    void findLatestProduct(){
        String targetProductNumber = "003";
        //given
        Product product1 = createProduct("001", HANDMADE, SELLING, "아메리카노", 4000);
        Product product2 = createProduct("002", HANDMADE, HOLD, "카페라떼", 4500);
        Product product3 = createProduct(targetProductNumber, HANDMADE, STOP_SELLING, "팥빙수", 7000);
        productRepository.saveAll(List.of(product1,product2,product3));

        //when
        String latestProductNumber = productRepository.findLatestProductNumber();

        //then
        assertThat(latestProductNumber).isEqualTo(targetProductNumber);
    }

    @DisplayName("가장 마지막으로 저장한 상품의 상품번호를 읽어올때, 상품이 하나도 없는 경우에는 null을 반환한다.")
    @Test
    void findLatestProductNumberWhenProductEmpty(){

        //when
        String latestProductNumber = productRepository.findLatestProductNumber();

        //then
        assertThat(latestProductNumber).isNull();
    }



    @DisplayName("상품번호 리스트로 상품들을 조회한다.")
    @Test
    void findAllByProductNumber(){
        //given
        Product product1 = createProduct("001", HANDMADE, SELLING, "아메리카노", 4000);

        Product product2 = createProduct("002", HANDMADE, HOLD, "카페라떼", 4500);

        Product product3 = createProduct("003", HANDMADE, STOP_SELLING, "팥빙수", 7000);

        productRepository.saveAll(List.of(product1,product2,product3));

        //when
        List<Product> products = productRepository.findAllByProductNumberIn(List.of("001","002"));

        //then
        assertThat(products).hasSize(2)
                .extracting("productNumber","name","sellingStatus")
                .containsExactlyInAnyOrder(
                        tuple("001","아메리카노",SELLING),
                        tuple("002","카페라떼",HOLD)
                );
    }

    private static Product createProduct(String productNumber, ProductType type, ProductSellingStatus sellingStatus, String name, int price) {
        return Product.builder()
                .productNumber(productNumber)
                .type(type)
                .sellingStatus(sellingStatus)
                .name(name)
                .price(price)
                .build();
    }


}