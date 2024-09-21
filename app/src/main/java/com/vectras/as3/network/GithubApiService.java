package com.vectras.as3.network;

import com.vectras.as3.model.GithubUser;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface GithubApiService {
    @GET("users/{username}")
    Call<GithubUser> getUser(@Path("username") String username);
}