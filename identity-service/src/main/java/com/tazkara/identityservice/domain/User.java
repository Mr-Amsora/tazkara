package com.tazkara.identityservice.domain;

import com.tazkara.identityservice.exception.InvalidRegistrationException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = true , length = 100)
    private String organizationName;

    private User(String fullName, String email, String passwordHash, Role role, String organizationName) {
        this.fullName = fullName;
        this.email = email;
        this.password = passwordHash;
        this.role = role;
        this.organizationName = organizationName;
    }

    public static User register(String fullName, String email, String passwordHash,
                                Role role, String organizationName) {
        boolean hasOrganization = organizationName != null && !organizationName.isBlank();
        if (role == Role.ORGANIZER && !hasOrganization) {
            throw new InvalidRegistrationException("Organization name is required for organizers");
        }
        String organization = (role == Role.ORGANIZER) ? organizationName.trim() : null;
        return new User(fullName, email, passwordHash, role, organization);
    }
}