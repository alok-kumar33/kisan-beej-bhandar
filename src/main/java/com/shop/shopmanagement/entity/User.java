package com.shop.shopmanagement.entity; // CHANGE THIS if your folder name is different

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users") // We use "users" because "user" is a reserved word in Postgres
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String fullName;
    private String phoneNumber;

    // These are the 3 roles you asked for
    public enum Role {
        ADMIN,
        OWNER,
        STAFF
    }
}