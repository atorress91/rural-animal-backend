package com.project.demo.rest.admin;

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

@RequestMapping("/admin")
@RestController
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TblRoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public TblUser createAdministrator(@RequestBody TblUser newAdminUser) {
        Optional<TblRole> optionalRole = roleRepository.findByTitle(RoleEnum.ADMIN);

        if (optionalRole.isEmpty()) {
            return null;
        }

        var user = new TblUser();
        user.setName(newAdminUser.getName());
        user.setLastName1(newAdminUser.getLastName1());
        user.setLastName2(newAdminUser.getLastName2());
        user.setIdentification(newAdminUser.getIdentification());
        user.setEmail(newAdminUser.getEmail());
        user.setPassword(passwordEncoder.encode(newAdminUser.getPassword()));
        user.setBirthDate(newAdminUser.getBirthDate());
        user.setPhoneNumber(newAdminUser.getPhoneNumber());
        user.setState(newAdminUser.getState());
        user.setRole(optionalRole.get());

        return userRepository.save(user);
    }
}
