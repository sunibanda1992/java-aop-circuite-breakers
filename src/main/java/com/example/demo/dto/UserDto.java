package com.example.demo.dto;

import com.example.demo.aspect.LogSensitive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password"}) // Exclude password from toString() method
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    
    @LogSensitive(showFirst = 3, showLast = 2)
    private String email;
    
    private int age;
    
    @LogSensitive
    private String password;
}
