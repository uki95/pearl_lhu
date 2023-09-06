package com.pits.auction.auctionBoard.service;

import com.pits.auction.auctionBoard.entity.BiddingPeriod;
import com.pits.auction.auctionBoard.entity.MusicAuction;
import com.pits.auction.auth.entity.Member;

import java.util.List;

public interface WishListService {

    String clickWishButton(String email, Long auctionId);

    long countByMemberNicknameAndAuctionId(Member member, MusicAuction auction);


}
