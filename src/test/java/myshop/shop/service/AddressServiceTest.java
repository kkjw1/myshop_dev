package myshop.shop.service;

import jakarta.persistence.EntityManager;
import myshop.shop.dto.address.AddAddressDto;
import myshop.shop.dto.address.ManageAddressDto;
import myshop.shop.dto.member.SignUpMemberDto;
import myshop.shop.entity.Address;
import myshop.shop.entity.member.Gender;
import myshop.shop.entity.member.Member;
import myshop.shop.repository.address.AddressRepository;
import myshop.shop.repository.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AddressServiceTest {

    @Autowired AddressService addressService;
    @Autowired AddressRepository addressRepository;
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired
    EntityManager em;

    @BeforeEach
    public void beforeInit() {
        SignUpMemberDto signUpMemberDto = SignUpMemberDto.builder()
                .id("id")
                .password("password")
                .email("email")
                .name("name")
                .telecom("telecom")
                .gender(Gender.MAN)
                .phoneNumber("phoneNumber")
                .build();
        memberService.signUp(signUpMemberDto);
    }

    @Test
    @Commit
    public void saveAddressTest() throws Exception {
        Member member = memberRepository.findById("id").orElse(null);
        Long no = member.getNo();
        AddAddressDto addAddressDto = AddAddressDto.builder()
                .addressName("addressName")
                .recipientName("recipientName")
                .phoneNumber("phoneNumber")
                .postcode("postcode")
                .roadAddress("roadAddress")
                .detailAddress("detailAddress")
                .isMain(true)
                .build();
        AddAddressDto addAddressDto2 = AddAddressDto.builder()
                .addressName("addressName2")
                .recipientName("recipientName2")
                .phoneNumber("phoneNumber2")
                .postcode("postcode2")
                .roadAddress("roadAddress2")
                .detailAddress("detailAddress2")
                .isMain(false)
                .build();


        addressService.saveAddress(no, addAddressDto);
        addressService.saveAddress(no, addAddressDto2);


        List<Address> all = addressRepository.findAll();
        for (Address address : all) {
            System.out.println("address = " + address);
        }
    }

    @Test
    @Commit
    public void findByMemberNoTest() throws Exception {
        //given
        Member member = memberRepository.findById("id").orElse(null);
        Long no = member.getNo();
        AddAddressDto addAddressDto = AddAddressDto.builder()
                .addressName("addressName")
                .recipientName("recipientName")
                .phoneNumber("phoneNumber")
                .postcode("postcode")
                .roadAddress("roadAddress")
                .detailAddress("detailAddress")
                .isMain(true)
                .build();
        AddAddressDto addAddressDto2 = AddAddressDto.builder()
                .addressName("addressName2")
                .recipientName("recipientName2")
                .phoneNumber("phoneNumber2")
                .postcode("postcode2")
                .roadAddress("roadAddress2")
                .detailAddress("detailAddress2")
                .isMain(false)
                .build();
        addressService.saveAddress(no, addAddressDto);
        addressService.saveAddress(no, addAddressDto2);

        List<Address> byMemberNo = addressRepository.findByMemberNo(no);
        for (Address address : byMemberNo) {
            System.out.println("address = " + address);
        }

        List<ManageAddressDto> addressesByMemberNo = addressService.getAddressesByMemberNo(no);
        for (ManageAddressDto addressDto : addressesByMemberNo) {
            System.out.println("addressDto = " + addressDto);
        }

    }

    @Test
    @DisplayName("메인이 있는 경우")
    public void mainUpdateTest1() throws Exception {
        //given
        Member member = memberRepository.findById("id").orElse(null);
        Long no = member.getNo();
        AddAddressDto addAddressDto = AddAddressDto.builder()
                .addressName("addressName")
                .recipientName("recipientName")
                .phoneNumber("phoneNumber")
                .postcode("postcode")
                .roadAddress("roadAddress")
                .detailAddress("detailAddress")
                .isMain(true)
                .build();
        AddAddressDto addAddressDto2 = AddAddressDto.builder()
                .addressName("addressName2")
                .recipientName("recipientName2")
                .phoneNumber("phoneNumber2")
                .postcode("postcode2")
                .roadAddress("roadAddress2")
                .detailAddress("detailAddress2")
                .isMain(false)
                .build();
        addressService.saveAddress(no, addAddressDto);
        addressService.saveAddress(no, addAddressDto2);

        //when
        addressService.mainUpdate(2L, no);

        //then
        List<Address> all = addressRepository.findAll();
        for (Address address : all) {
            System.out.println("address = " + address);
        }
    }

    @Test
    @DisplayName("메인이 없는 경우")
    public void mainUpdateTest2() throws Exception {
        //given
        Member member = memberRepository.findById("id").orElse(null);
        Long no = member.getNo();
        AddAddressDto addAddressDto = AddAddressDto.builder()
                .addressName("addressName")
                .recipientName("recipientName")
                .phoneNumber("phoneNumber")
                .postcode("postcode")
                .roadAddress("roadAddress")
                .detailAddress("detailAddress")
                .isMain(false)
                .build();
        AddAddressDto addAddressDto2 = AddAddressDto.builder()
                .addressName("addressName2")
                .recipientName("recipientName2")
                .phoneNumber("phoneNumber2")
                .postcode("postcode2")
                .roadAddress("roadAddress2")
                .detailAddress("detailAddress2")
                .isMain(false)
                .build();
        addressService.saveAddress(no, addAddressDto);
        addressService.saveAddress(no, addAddressDto2);

        //when
        addressService.mainUpdate(2L, no);

        //then
        List<Address> all = addressRepository.findAll();
        for (Address address : all) {
            System.out.println("address = " + address);
        }
    }

    @Test
    public void deleteAddressTest() throws Exception {
        //given
        Member member = memberRepository.findById("id").orElse(null);
        Long no = member.getNo();
        AddAddressDto addAddressDto = AddAddressDto.builder()
                .addressName("addressName")
                .recipientName("recipientName")
                .phoneNumber("phoneNumber")
                .postcode("postcode")
                .roadAddress("roadAddress")
                .detailAddress("detailAddress")
                .isMain(true)
                .build();
        AddAddressDto addAddressDto2 = AddAddressDto.builder()
                .addressName("addressName2")
                .recipientName("recipientName2")
                .phoneNumber("phoneNumber2")
                .postcode("postcode2")
                .roadAddress("roadAddress2")
                .detailAddress("detailAddress2")
                .isMain(false)
                .build();
        addressService.saveAddress(no, addAddressDto);
        addressService.saveAddress(no, addAddressDto2);

        //when
        addressService.deleteAddress(1L);

        //then
        List<Address> byMemberNo = addressRepository.findByMemberNo(no);
        assertThat(byMemberNo.size()).isEqualTo(1);
        for (Address address : byMemberNo) {
            System.out.println("address = " + address);
        }
    }

    @Test
    public void findByNoTest() throws Exception {
        //given
        Member member = memberRepository.findById("id").orElse(null);
        Long no = member.getNo();
        AddAddressDto addAddressDto = AddAddressDto.builder()
                .addressName("addressName")
                .recipientName("recipientName")
                .phoneNumber("phoneNumber")
                .postcode("postcode")
                .roadAddress("roadAddress")
                .detailAddress("detailAddress")
                .isMain(true)
                .build();
        AddAddressDto addAddressDto2 = AddAddressDto.builder()
                .addressName("addressName2")
                .recipientName("recipientName2")
                .phoneNumber("phoneNumber2")
                .postcode("postcode2")
                .roadAddress("roadAddress2")
                .detailAddress("detailAddress2")
                .isMain(false)
                .build();
        addressService.saveAddress(no, addAddressDto);
        addressService.saveAddress(no, addAddressDto2);

        //when
        Address address = addressRepository.findByNo(1L).orElse(null);

        //then
        System.out.println("address = " + address);
    }

}