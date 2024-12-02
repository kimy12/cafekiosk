package sample.cafekiosk.spring.api.service.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sample.cafekiosk.spring.IntegrationTestSupport;
import sample.cafekiosk.spring.api.controller.order.request.OrderCreateRequest;
import sample.cafekiosk.spring.api.service.order.response.OrderResponse;
import sample.cafekiosk.spring.domain.Product;
import sample.cafekiosk.spring.domain.ProductRepository;
import sample.cafekiosk.spring.domain.ProductType;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.orderproduct.OrderProductRepository;
import sample.cafekiosk.spring.domain.stock.Stock;
import sample.cafekiosk.spring.domain.stock.StockRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static sample.cafekiosk.spring.domain.ProductSellingStatus.*;
import static sample.cafekiosk.spring.domain.ProductType.*;

//@DataJpaTest
//@ActiveProfiles("test")
//@SpringBootTest
//@Transactional
class OrderServiceTest extends IntegrationTestSupport {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private StockRepository stockRepository;

    /**
     * 데이터 클렌징 작업.
     * 테스트가 끝날때마다 delete를 해줌.
     * (없으면 테스트가 단독 으로는 성공 하지만 여러 테스트 함께 하면 실패 하게 됨)
     *
     * 클랜징 안하고 @Transactional 걸면? 롤백 되서 성공 함.
     * transactional 은 사실 편리한게 맞음. 더티체킹으로 변경감지를 해줌.
     *
     * but 문제점 ? 실제 서비스에서는 프로덕션 코드에 transactional이 있는지 확인하지 못함 - 부작용 인지 빌요
     */
    @AfterEach
    void tearDown() {
        orderProductRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        stockRepository.deleteAllInBatch();
    }

    @DisplayName("주문번호 리스트를 받아 주문을 생성한다.")
    @Test
    void createOrder(){

        //given
        LocalDateTime registeredDateTime = LocalDateTime.now();
        Product product1 = createProduct(HANDMADE,"001",1000);
        Product product2 = createProduct(HANDMADE,"002",3000);
        Product product3 = createProduct(HANDMADE,"003",5000);

        productRepository.saveAll(List.of(product1,product2,product3));

        OrderCreateRequest request= OrderCreateRequest.builder()
                .productNumbers(List.of("001","002"))
                .build();
        //when
        OrderResponse orderResponse = orderService.createOrder(request.toServiceRequest(), registeredDateTime);

        //then
        assertThat(orderResponse.getId()).isNotNull();
        assertThat(orderResponse)
                .extracting("registeredDateTime","totalPrice")
                .contains(registeredDateTime, 4000);
        assertThat(orderResponse.getProducts()).hasSize(2)
                .extracting("productNumber", "price")
                .containsExactlyInAnyOrder(
                    tuple("001",1000),
                    tuple("002",3000)
                );
    }



    @DisplayName("중복되는 상품번호 리스트로 주문을 생성할 수 있다.")
    @Test
    void createOrderWithDuplicateProductNumber(){
        //given
        LocalDateTime registeredDateTime = LocalDateTime.now();
        Product product1 = createProduct(HANDMADE,"001",1000);
        Product product2 = createProduct(HANDMADE,"002",3000);
        Product product3 = createProduct(HANDMADE,"003",5000);

        productRepository.saveAll(List.of(product1,product2,product3));

        OrderCreateRequest request= OrderCreateRequest.builder()
                .productNumbers(List.of("001","001"))
                .build();
        //when
        OrderResponse orderResponse = orderService.createOrder(request.toServiceRequest(), registeredDateTime);

        //then
        assertThat(orderResponse.getId()).isNotNull();
        assertThat(orderResponse)
                .extracting("registeredDateTime","totalPrice")
                .contains(registeredDateTime,2000);
        assertThat(orderResponse.getProducts()).hasSize(2)
                .extracting("productNumber", "price")
                .containsExactlyInAnyOrder(
                        tuple("001",1000),
                        tuple("001",1000)
                );
    }

    @DisplayName("재고와 관련된 상품이 포함 되어 있는 주문번호 리스트를 받아 주문을 생성한다.")
    @Test
    void createOrderWithStock(){

        //given
        LocalDateTime registeredDateTime = LocalDateTime.now();
        Product product1 = createProduct(BOTTLE,"001",1000);
        Product product2 = createProduct(BAKERY,"002",3000);
        Product product3 = createProduct(HANDMADE,"003",5000);
        productRepository.saveAll(List.of(product1,product2,product3));

        Stock stock1 = Stock.create("001",2);
        Stock stock2 = Stock.create("002",2);

        stockRepository.saveAll(List.of(stock1,stock2));

        OrderCreateRequest request= OrderCreateRequest.builder()
                .productNumbers(List.of("001","001","002","003"))
                .build();
        //when
        OrderResponse orderResponse = orderService.createOrder(request.toServiceRequest(), registeredDateTime);

        //then
        assertThat(orderResponse.getId()).isNotNull();
        assertThat(orderResponse)
                .extracting("registeredDateTime","totalPrice")
                .contains(registeredDateTime, 10000);
        assertThat(orderResponse.getProducts()).hasSize(4)
                .extracting("productNumber", "price")
                .containsExactlyInAnyOrder(
                        tuple("001",1000),
                        tuple("001",1000),
                        tuple("002",3000),
                        tuple("003",5000)
                );

        List<Stock> stocks = stockRepository.findAll();

        assertThat(stocks).hasSize(2)
                .extracting("productNumber","quantity")
                .containsExactlyInAnyOrder(
                        tuple("001",0),
                        tuple("002",1)
                );
    }

    @DisplayName("재고가 부족한 상품으로 주문을 생성하려는 경우 예외가 발생한다.")
    @Test
    void createOrderWithNoStock(){

        //given
        LocalDateTime registeredDateTime = LocalDateTime.now();
        Product product1 = createProduct(BOTTLE,"001",1000);
        Product product2 = createProduct(BAKERY,"002",3000);
        Product product3 = createProduct(HANDMADE,"003",5000);
        productRepository.saveAll(List.of(product1,product2,product3));

        Stock stock1 = Stock.create("001",2);
        Stock stock2 = Stock.create("002",2);
        stock1.deductQuantity(1); // todo

        stockRepository.saveAll(List.of(stock1,stock2));

        OrderCreateRequest request= OrderCreateRequest.builder()
                .productNumbers(List.of("001","001","002","003"))
                .build();
        //when //then

        assertThatThrownBy(() -> orderService.createOrder(request.toServiceRequest(), registeredDateTime))
                .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("재고가 부족한 상품이 있습니다.");

    }

    // product 를 생성하기 위한 메서드
    private Product createProduct(ProductType type, String productNumber, int price){
        return Product.builder()
                .type(type)
                .productNumber(productNumber)
                .price(price)
                .sellingStatus(SELLING)
                .name("메뉴 이름")
                .build();
    }
}