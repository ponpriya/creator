package com.poomaalai.entity;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CREATOR")
@Getter
@Setter
public class Creator extends Auditable implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;


    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "first name")
    private String firstName;

    @Column(name = "last name")
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;       

    @Column(name = "password")
    private String password;
    
    @Column(name = "address")
    private String address;

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    @Column(name = "zipcode")
    private String zipcode;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getUsername() {
        return email;
    }

    

  


}