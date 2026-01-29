package com.project.demo.logic.entity.user;

import com.project.demo.logic.entity.direction.TblDirection;
import com.project.demo.logic.entity.role.TblRole;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_User")
public class TblUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "User_Id", nullable = false)
    private Long id;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Last_Name1", nullable = false)
    private String lastName1;

    @Column(name = "Last_Name2")
    private String lastName2;

    @Column(name = "Identification", nullable = false)
    private String identification;

    @Column(name = "VCO", nullable = true)
    private String vco;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "Password", nullable = false)
    private String password;

    @Column(name = "Birth_Date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "Phone_Number", nullable = false)
    private String phoneNumber;

    @Column(name = "State", nullable = false, length = 10)
    private String state = "Inactive";

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "Direction_Id", nullable = true)
    private TblDirection direction;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "Role_Id", nullable = false)
    private TblRole role;

    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;

    @Column(name = "lock_time")
    private Long lockTime; //

    @Column(length = 1000)
    private String googleRefreshToken;

    @Column(length = 1000)
    private String googleAccessToken;

    @Column
    private LocalDateTime tokenExpiration;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.getTitle().toString());
        return List.of(authority);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName1() {
        return lastName1;
    }

    public void setLastName1(String lastName1) {
        this.lastName1 = lastName1;
    }

    public String getLastName2() {
        return lastName2;
    }

    public void setLastName2(String lastName2) {
        this.lastName2 = lastName2;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getVco() {
        return vco;
    }

    public void setVco(String vco) {
        this.vco = vco;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public TblDirection getDirection() {
        return direction;
    }

    public void setDirection(TblDirection direction) {
        this.direction = direction;
    }

    public TblRole getRole() {
        return role;
    }

    public TblUser setRole(TblRole role) {
        this.role = role;
        return this;
    }

    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public Long getLockTime() {
        return lockTime;
    }

    public void setLockTime(Long lockTime) {
        this.lockTime = lockTime;
    }

    public String getGoogleRefreshToken() {
        return googleRefreshToken;
    }

    public void setGoogleRefreshToken(String googleRefreshToken) {
        this.googleRefreshToken = googleRefreshToken;
    }

    public String getGoogleAccessToken() {
        return googleAccessToken;
    }

    public void setGoogleAccessToken(String googleAccessToken) {
        this.googleAccessToken = googleAccessToken;
    }

    public LocalDateTime getTokenExpiration() {
        return tokenExpiration;
    }

    public void setTokenExpiration(LocalDateTime tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
    }
}