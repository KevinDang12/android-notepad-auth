package com.example.notepadauth;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetrofitInterface {

    @POST("/api/notes")
    Call<HashMap<String, String>> saveNotes(@Body HashMap<String, String> map);

}
