package sample.cafekiosk.spring.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sample.cafekiosk.spring.IntegrationTestSupport;
import sample.cafekiosk.spring.domain.Product;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static sample.cafekiosk.spring.domain.ProductSellingStatus.SELLING;
import static sample.cafekiosk.spring.domain.ProductType.HANDMADE;

//@ActiveProfiles("test")
//@DataJpaTest
@Transactional
class OrderRepositoryTest extends IntegrationTestSupport {

    @Autowired
    OrderRepository orderRepository;

    @DisplayName("해당일자에 주문완료된 주문들을 가져온다.")
    @Test
    void findAllByDateAndStatus(){
        //given
        LocalDateTime registeredDateTime = LocalDateTime.now();
        List<Product> products = List.of(
                createProduct("001", 1000),
                createProduct("002", 2000)
        );

//        Order.builder()
//                .id(1L)
//                .orderStatus(OrderStatus.RECEIVED)
//                .registeredDateTime(registeredDateTime)
//                .totalPrice("3000")
//                .build();

        //when
        Order order = Order.create(products, registeredDateTime);



        //then
        assertThat(order.getRegisteredDateTime()).isEqualTo(registeredDateTime);
    }

    private Product createProduct(String productNumber, int price){
        return Product.builder()
                .type(HANDMADE)
                .productNumber(productNumber)
                .price(price)
                .sellingStatus(SELLING)
                .name("메뉴 이름")
                .build();
    }
}