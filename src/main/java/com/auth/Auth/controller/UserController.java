package com.auth.Auth.controller;

import com.auth.Auth.dto.UpdateUser;
import com.auth.Auth.dto.UserDTO;
import com.auth.Auth.services.UserServices;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private  final UserServices userServices;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(userServices.createUser(userDTO));
    }

    @GetMapping
    public ResponseEntity<Iterable<UserDTO>> getAllUser(){
        return ResponseEntity.ok(userServices.getAllUser());
    }

    @GetMapping("/email/{email}")
    public  ResponseEntity<UserDTO> findUserByEmail(@PathVariable String email){
        return ResponseEntity.ok(userServices.findUserByEmail(email));
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<UserDTO> deleteUser(@PathVariable String id){
        return ResponseEntity.ok(userServices.deleteUser(id));
    }

    @PutMapping("/{user_id}")
    public ResponseEntity<UserDTO> updateUser(@RequestBody UpdateUser updatedUser, @PathVariable String user_id){
        return ResponseEntity.status(HttpStatus.OK).body(userServices.updateUser(updatedUser, user_id));
    }

}
