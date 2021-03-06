package com.crossit.service;

import com.crossit.dao.MemberDao;
import com.crossit.entity.Member;
import com.crossit.entity.SignUpForm;
import com.crossit.entity.UserMember;
import com.crossit.repository.MemberRepository;
import com.crossit.setting.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService implements UserDetailsService {

	@Autowired
	MemberDao memberDao;

	private final PasswordEncoder passwordEncoder;
	private final MemberRepository memberRepository;
	private final JavaMailSender javaMailSender;

	public int signIn(Member member) {

		int result = memberDao.signIn(member);

		if (result > 0) {
			System.out.println("로그인 성공");

		}

		return result;

	}

	public Member findMemberByNickName(String nickName) {
		Member member = memberDao.findMemberByNickName(nickName);
		return member;
	}

	public Member processNewAccount(SignUpForm signUpForm) {
		Member newMember = saveNewAccount(signUpForm);

		newMember.generateEmailCheckToken();

		sendSignUpConfirmEmail(newMember);
		return newMember;
	}

	public void login(Member member) {

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
			new UserMember(member),
			member.getPassword(),
			List.of(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(token);

	}

	public void memberUpdate(Member member) {

		memberDao.memberUpdate(member);

	}

	private Member saveNewAccount(SignUpForm signUpForm) {
		Member member = Member.builder()
			.nickname(signUpForm.getNickname())
			.email(signUpForm.getEmail())
			.password(passwordEncoder.encode(signUpForm.getPassword()))
			.job((long) signUpForm.getJob())
			.build();

		Member newMember = memberRepository.save(member);
		return newMember;
	}

	public void sendSignUpConfirmEmail(Member newMember) {
		SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
		simpleMailMessage.setTo(newMember.getEmail());
		simpleMailMessage.setSubject("CrossIt, 회원 가입 인증메일 입니다.");
		simpleMailMessage.setText("/check-email-token?token=" + newMember.getEmailCheckToken() +
			"&email=" + newMember.getEmail());
		javaMailSender.send(simpleMailMessage);
		System.out.println("/check-email-token?token=" + newMember.getEmailCheckToken() +
			"&email=" + newMember.getEmail());
	}

	@Transactional(readOnly = true)
	@Override
	public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {

		Member member = memberRepository.findByEmail(emailOrNickname);

		if (member == null) {
			member = memberRepository.findByNickname(emailOrNickname);
		}

		if (member == null) {
			throw new UsernameNotFoundException(emailOrNickname);
		}

		return new UserMember(member);
	}

	public void completeSignUp(Member member) {
		member.completeSignUp();
		login(member);
	}

	public void updateProfile(Member member, Profile profile) {
		member.setIntroduction(profile.getIntroduction());
		member.setLocation(profile.getLocation());
		member.setContact(profile.getContact());
		member.setProfileImage(profile.getProfileImage());
		memberRepository.save(member);
	}

	public String getMemberProfile(String nickname) {
		Member member = memberRepository.findByNickname(nickname);
		return member.getProfileImage();
	}
}
