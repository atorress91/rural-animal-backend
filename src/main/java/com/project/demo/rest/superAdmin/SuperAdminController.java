package com.project.demo.rest.superAdmin;

import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.TblRole;
import com.project.demo.logic.entity.role.TblRoleRepository;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RequestMapping("/superAdmin")
@RestController
public class SuperAdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TblRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public TblUser createAdministrator(@RequestBody TblUser newSuperAdminUser) {
        Optional<TblRole> optionalRole = roleRepository.findByTitle(RoleEnum.SUPER_ADMIN);

        if (optionalRole.isEmpty()) {
            return null;
        }

        var user = new TblUser();
        user.setName(newSuperAdminUser.getName());
        user.setLastName1(newSuperAdminUser.getLastName1());
        user.setLastName2(newSuperAdminUser.getLastName2());
        user.setIdentification(newSuperAdminUser.getIdentification());
        user.setEmail(newSuperAdminUser.getEmail());
        user.setPassword(passwordEncoder.encode(newSuperAdminUser.getPassword()));
        user.setBirthDate(newSuperAdminUser.getBirthDate());
        user.setPhoneNumber(newSuperAdminUser.getPhoneNumber());
        user.setState(newSuperAdminUser.getState());
        user.setRole(optionalRole.get());

        return userRepository.save(user);
    }
}
