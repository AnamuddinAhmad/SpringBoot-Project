package com.auth.Auth.services.Implementation;

import com.auth.Auth.dto.UpdateUser;
import com.auth.Auth.dto.UserDTO;
import com.auth.Auth.entity.Users;
import com.auth.Auth.exception.ValidationError;
import com.auth.Auth.repo.UserRepo;
import com.auth.Auth.services.UserServices;
import com.auth.Auth.utils.Provider;
import com.auth.Auth.utils.UserHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserServices {

    private final UserRepo userRepo;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        System.out.println("Starting create user");
        if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
            System.out.println("UserServiceImpl :: createUser() -> Email is required.");
            throw ValidationError.badRequest("Email is required.");
        }

        if (userRepo.existsByEmail(userDTO.getEmail())) {
            System.out.println("UserServiceImpl :: createUser() -> User already exist.");
            throw new ValidationError("User already exist.", HttpStatus.CONFLICT);
        }

        Users user = modelMapper.map(userDTO, Users.class);

        // Checking provider — default to LOCAL if the caller didn't specify one.
        user.setProvider(userDTO.getProvider() != null ? userDTO.getProvider() : Provider.LOCAL);

        // Respect whatever enabled value the caller (e.g. AuthServiceImpl)
        // has already decided — default to true only if truly unspecified.
        // This is intentionally NOT trusted blindly from raw client JSON;
        // callers like AuthServiceImpl.registerUser() are responsible for
        // forcing the correct value before calling createUser().
        user.setEnabled(userDTO.getEnabled() != null ? userDTO.getEnabled() : true);

        //Assing a guest role
        //TODO:
        //Createing User
        Users saveUser = userRepo.save(user);

        return modelMapper.map(saveUser, UserDTO.class);
    }

    @Override
    public UserDTO findUserByEmail(String email) {
        System.out.println("Starting find user");
        if (email == null || email.isBlank()) {
            System.out.println("UserServiceImpl :: findUserByEmail() -> " + email);
            throw ValidationError.badRequest("Email is required.");
        }
        Users user = userRepo.findByEmail(email).orElseThrow(() -> ValidationError.notFound("User not found."));
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    public UserDTO getFindById(String id) {
        return null;
    }

    @Override
    public UserDTO updateUser(UpdateUser updatedUser, String userId) {
        System.out.println("Starting update user");

        if (userId.isBlank()) {
            System.out.println("UserServiceImpl :: updateUser() -> " + userId);
            throw ValidationError.badRequest("Invalid User Id.");
        }
        UUID uid = UserHelper.parseUUID(userId);
        //Finding Existig User.
        Users existingUser = userRepo.findById(uid).orElseThrow(() -> {
            System.out.println("UserServiceImpl :: updateUser() -> " + uid + " Invalid UUID");
            return ValidationError.notFound("User not found kindly provide the Valid existingUser");
        });

        existingUser.setUpdatedAt(Instant.now());
        if (updatedUser.getName() != null) existingUser.setName(updatedUser.getName());

        //TODO: Need to change the password update logic.
        if (updatedUser.getPassword() != null) existingUser.setPassword(updatedUser.getPassword());
        if (updatedUser.getImage() != null) existingUser.setImage(updatedUser.getImage());
        if (updatedUser.getProvider() != null) existingUser.setProvider(updatedUser.getProvider());
        if (updatedUser.getEnabled() != null) existingUser.setEnabled(updatedUser.getEnabled());


        Users newUpdatedUser = userRepo.save(existingUser);

        return modelMapper.map(newUpdatedUser, UserDTO.class);
    }

    @Override
    public UserDTO deleteUser(String id) {
        System.out.println("Starting Delete User");
        if (id.isBlank() || id == null) {
            throw ValidationError.forbidden("Id is required.");
        }
        UUID uid = UserHelper.parseUUID(id);

        Users user = userRepo.findById(uid).orElseThrow(() -> ValidationError.notFound("User not found."));
        userRepo.delete(user);
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    public Iterable<UserDTO> getAllUser() {
        System.out.println("Starting getAll User");
        return userRepo.
                findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();
    }
}