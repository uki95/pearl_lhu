package com.pits.auction.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;


/*  음악장르
테이블 : music_genre

컬럼 :
    id : 장르 번호 (Primary Key, Auto Increment)
    name : 장르명 (Not Null)
*/
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "music_genre")
public class MusicGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // 해당 장르의 경매글 정보
    @OneToMany(mappedBy = "genre")
    private List<MusicAuction> genreAuctions;
}