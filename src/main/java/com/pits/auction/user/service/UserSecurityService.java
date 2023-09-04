package com.pits.auction.user.service;


import com.pits.auction.auth.entity.Member;
import com.pits.auction.user.repository.UserRepository;
import com.pits.auction.user.validation.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//spring security에 등록할 클래스
@RequiredArgsConstructor
@Service
public class UserSecurityService implements UserDetailsService {

    private final UserRepository userRepository;

    //사용자이름으로 (비밀번호)조회
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Member> optionalMember = userRepository.findByEmail(email);
        if(!optionalMember.isPresent()){
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
        }

        Member siteUser = optionalMember.get();
        List<GrantedAuthority> authorities = new ArrayList<>();
        //nickname "admin"을 포함하면
        if (siteUser.getNickname().contains("admin")) {
            authorities.add(new SimpleGrantedAuthority(UserRole.ADMIN.getValue()));
            System.out.println("UserRole.ADMIN.getValue())"+UserRole.ADMIN.getValue());
        } else {
            authorities.add(new SimpleGrantedAuthority(UserRole.USER.getValue()));
            System.out.println("UserRole.USER.getValue())"+UserRole.USER.getValue());
        }
        //사용자User객체리턴
        return new User(email, siteUser.getPassword(), authorities);
        //스크링시큐리티는  loadUserByUsername()에 의해 리턴되는 User객체의 비밀번호가
        // 화면으로부터 입력한 비번와 일치하는지 검사하는 로직이 내부적으로 존재
    }
}
