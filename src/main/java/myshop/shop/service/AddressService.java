package myshop.shop.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import myshop.shop.dto.address.AddAddressDto;
import myshop.shop.dto.address.ManageAddressDto;
import myshop.shop.dto.address.UpdateAddressDto;
import myshop.shop.entity.Address;
import myshop.shop.entity.member.Member;
import myshop.shop.repository.address.AddressRepository;
import myshop.shop.repository.member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final MemberRepository memberRepository;
    private final EntityManager em;


    /**
     * 주소 저장
     */
    public void saveAddress(Long memberNo, AddAddressDto AddAddressDto) {
        if (AddAddressDto.getIsMain()) {
            addressRepository.updateIsMainToFalse(memberNo);
        }
        Member memberProxy = memberRepository.getReferenceById(memberNo);
        Address address = new Address(memberProxy,
                AddAddressDto.getAddressName(),
                AddAddressDto.getRecipientName(),
                AddAddressDto.getPhoneNumber(),
                AddAddressDto.getPostcode(),
                AddAddressDto.getRoadAddress(),
                AddAddressDto.getDetailAddress(),
                AddAddressDto.getIsMain());
        addressRepository.save(address);
    }



    /**
     * 주소리스트 반환(ManageAddressDto)
     */
    public List<ManageAddressDto> getAddressesByMemberNo(Long memberNo) {
        List<Address> findAddresses = addressRepository.findByMemberNoOrderByIsMainDesc(memberNo);
        return findAddresses.stream()
                .map(address -> ManageAddressDto.builder()
                    .addressNo(address.getNo())
                    .addressName(address.getAddressName())
                    .recipientName(address.getRecipientName())
                    .phoneNumber(address.getPhoneNumber())
                    .postcode(address.getPostcode())
                    .roadAddress(address.getRoadAddress())
                    .detailAddress(address.getDetailAddress())
                    .isMain(address.getIsMain())
                    .build())
                .collect(toList());
    }


    /**
     * 주소 반환
     */
    public ManageAddressDto getAddressByMemberNo(Long memberNo) {
        Address address = addressRepository.findByMemberIsMain(memberNo).orElse(null);
        if (address == null) {
            return null;
        }
        return ManageAddressDto.builder()
            .addressNo(address.getNo())
            .addressName(address.getAddressName())
            .recipientName(address.getRecipientName())
            .phoneNumber(address.getPhoneNumber())
            .postcode(address.getPostcode())
            .roadAddress(address.getRoadAddress())
            .detailAddress(address.getDetailAddress())
            .isMain(address.getIsMain())
            .build();
    }



    /**
     * 기본 배송지로 변경
     */
    public int mainUpdate(Long addressNo, Long memberNo) {
        int result1 = addressRepository.updateIsMainToFalse(memberNo);
        int result2 = addressRepository.updateIsMainToTrue(addressNo);
        return result1 + result2;
    }


    /**
     * 주소 삭제
     */
    public int deleteAddress(Long addressNo) {
        return addressRepository.deleteAddressByNo(addressNo);
    }


    /**
     * 주소 수정
     */
    public Address addressModify(UpdateAddressDto updateAddressDto) {
        em.flush();
        em.clear();

        Address address = addressRepository.findByNo(updateAddressDto.getAddressNo()).orElse(null);
        address.updateAddressName(updateAddressDto.getAddressName());
        address.updateRecipientName(updateAddressDto.getRecipientName());
        address.updatePhoneNumber(updateAddressDto.getPhoneNumber());
        address.updatePostcode(updateAddressDto.getPostcode());
        address.updateRoadAddress(updateAddressDto.getRoadAddress());
        address.updateDetailAddress(updateAddressDto.getDetailAddress());

        return address;
    }
}
