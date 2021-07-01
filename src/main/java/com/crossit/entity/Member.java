package com.crossit.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Member {

	@Id @GeneratedValue
	private Long id;

	@Column(unique = true)
	private String email;

	@Column(unique = true)
	private String nickname;

	private String password;

	private boolean emailVerified;

	private String emailCheckToken;

	private LocalTime joinedAt;

	private Long role;

	private Long career;

	private Long belonging;

	@Lob
	@Basic(fetch = FetchType.EAGER)
	private String profileImage;

	private String introduction;

	private String portfolio;

	private boolean studyCreatedByEmail;

	private boolean studyCreatedByWeb;

	private boolean studyEnrollmentResultByEmail;

	private boolean studyEnrollmentResultByWeb;

	private boolean studyUpdatedByEmail;

	private boolean studyUpdatedByWeb;

}
