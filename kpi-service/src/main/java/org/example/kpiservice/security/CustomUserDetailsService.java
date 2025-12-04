package org.example.kpiservice.security;

import lombok.RequiredArgsConstructor;
import org.example.kpiservice.secondary.entity.Employee;
import org.example.kpiservice.secondary.repository.SecondaryEmployeeRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SecondaryEmployeeRepository secondaryEmployeeRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Employee employee = secondaryEmployeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return User.builder()
                .username(employee.getEmail())
                .password("")
                .authorities(new ArrayList<>())
                .build();
    }

    public Employee getEmployeeByEmail(String email) {
        return secondaryEmployeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
