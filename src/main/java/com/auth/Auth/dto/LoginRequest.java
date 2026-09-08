package com.auth.Auth.dto;

import lombok.Getter;
import lombok.Setter;


public record LoginRequest (String email, String password){

}
