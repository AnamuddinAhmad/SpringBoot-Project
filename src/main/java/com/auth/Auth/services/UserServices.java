package com.auth.Auth.services;

import com.auth.Auth.dto.UpdateUser;
import com.auth.Auth.dto.UserDTO;

public interface UserServices {
    UserDTO createUser (UserDTO userDTO);

    UserDTO findUserByEmail(String email);

    UserDTO getFindById(String id);

    UserDTO updateUser (UpdateUser updateUser, String Userid);

    UserDTO deleteUser (String id);

    Iterable<UserDTO> getAllUser();
}
