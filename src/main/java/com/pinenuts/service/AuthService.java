package com.pinenuts.service;

import com.pinenuts.dto.*;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(String refreshToken);
    void logout();
    UserInfoResponse getCurrentUserInfo();
}
