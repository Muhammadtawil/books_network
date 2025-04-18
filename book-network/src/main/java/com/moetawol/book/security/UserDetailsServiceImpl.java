package com.moetawol.book.security;

import com.moetawol.book.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // Marks this class as a Spring Service Component (automatically registered as a bean)
@RequiredArgsConstructor // Generates a constructor for the final field 'repository'
public class UserDetailsServiceImpl implements UserDetailsService {

    // 1. Inject UserRepository to fetch user details from the database
    private final UserRepository repository;

    @Override
    @Transactional // 2. Ensure this method runs inside a transaction (good for lazy loading or DB consistency)
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        // 3. Use the repository to find a user by email
        // 4. If user is found, return it as UserDetails
        // 5. If not, throw UsernameNotFoundException
        return repository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
