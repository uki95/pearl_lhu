package com.pits.auction.auctionBoard.service;


import com.pits.auction.auctionBoard.component.BiddingMapper;
import com.pits.auction.auctionBoard.dto.BiddingDTO;
import com.pits.auction.auctionBoard.entity.Bidding;
import com.pits.auction.auctionBoard.entity.MusicAuction;
import com.pits.auction.auctionBoard.repository.BiddingRepository;
import com.pits.auction.auctionBoard.repository.MusicAuctionRepository;
import com.pits.auction.auth.entity.Member;
import com.pits.auction.auth.repository.MemberRepository;
import com.pits.auction.global.exception.InsufficientBiddingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BiddingServiceImpl implements BiddingService{

    private final BiddingRepository biddingRepository;
    private final MemberRepository memberRepository;
    private final MusicAuctionRepository musicAuctionRepository;
    private final ModelMapper modelMapper;
    private final BiddingMapper biddingMapper;


    /* 입찰 DTO -> Entity 형변환 (첫 생성에만 사용 - 사실상 입찰은 변경이 불가능 / BidTime, Status null값 받아오면 초기값 지정) */
    @Override
    @Transactional
    public Bidding createBidding(BiddingDTO biddingDTO) {
        Optional<Member> memberOptional = memberRepository.findByNickname(biddingDTO.getBidder());
        Optional<MusicAuction> musicAuctionOptional = musicAuctionRepository.findById(biddingDTO.getAuctionId());

        // 입력된 데이터에 맞는 정보를 불러왔다면 기능 수행
        if (memberOptional.isPresent() && musicAuctionOptional.isPresent()) {
            Member member = memberOptional.get();
            MusicAuction musicAuction = musicAuctionOptional.get();

            // 입찰가가 시작입찰가 보다 낮으면
            if(musicAuction.getStartingBid() > biddingDTO.getPrice()){
                throw new InsufficientBiddingException("경매하신 물품의 시작 입찰가는 " + musicAuction.getStartingBid() + "입니다.");
            }

            long currentTimeMillis = System.currentTimeMillis();
            long specificTimeMillis = musicAuction.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            System.out.println(specificTimeMillis-currentTimeMillis);
            // 현재 시간이 마감 시간을 넘어간 물품에 경매를 할 경우
            if (specificTimeMillis < currentTimeMillis){
                throw new InsufficientBiddingException("경매하신 물품은 마감되었습니다.");
            }

            // 입찰 시 입찰 종료 시간이 10분 이내 라면 입찰 종료 시간 증가
            if (specificTimeMillis - currentTimeMillis < 60000){

                // 현재시간에 10분을 추가한 정보로 업데이트
                LocalDateTime newDateTime = LocalDateTime.now().plusMinutes(10);
                System.out.println(newDateTime);
                musicAuction.setEndTime(newDateTime);
                musicAuctionRepository.save(musicAuction);
            }

            return biddingRepository.save(Bidding.builder()
                    .id(biddingDTO.getId())
                    .bidder(member)
                    .auctionId(musicAuction)
                    .price(biddingDTO.getPrice())
                    .bidTime(biddingDTO.getBidTime() != null ? biddingDTO.getBidTime() : LocalDateTime.now())
                    .status(biddingDTO.getStatus() != null ? biddingDTO.getStatus() : "진행")
                    .build());
        } else {
            throw new InsufficientBiddingException("경매중 오류가 발생했습니다.");
        }
    }



    /* ID값으로 입찰기록 가져오기 */
    @Override
    public BiddingDTO findById(Long id) {
        Optional<Bidding> optionalBidding = biddingRepository.findById(id);
        if (optionalBidding.isPresent()){
            Bidding bidding = optionalBidding.get();
            return biddingMapper.convertToDTO(bidding);
        }
        return null;
    }


    @Override
    /* 경매 물품(음악)에 따른 입찰 목록 */
    public List<Bidding> getAuctionBiddingsById(Long auctionId) {
        MusicAuction musicAuction = musicAuctionRepository.findById(auctionId)
                .orElseThrow(() -> new EntityNotFoundException("Auction not found with id: " + auctionId));

        return musicAuction.getAuctionBiddings();
    }

    /* 입찰 전체목록 */
    @Override
    public List<Bidding> getAuctionBiddings() {
        return biddingRepository.findAll();
    }


    /* 경매 물품 최대 입찰가 */
    @Override
    public Long getMaxBidPriceForAuction(Long auctionId) {
        Optional<Long> maxBidPrice = biddingRepository.findMaxPriceByAuctionId(auctionId);
        return maxBidPrice.orElse(0L); // 0L 또는 원하는 기본 값으로 변경
    }



}
