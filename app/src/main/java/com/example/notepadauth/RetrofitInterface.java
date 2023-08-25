package com.example.notepadauth;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RetrofitInterface {

    @GET("/api/status")
    Call<Void> getStatus();

    @POST("/api/notes")
    Call<Void> saveNotes(@Body HashMap<String, String> map);

    @GET("/api/notes")
    Call<User[]> getNotes();

    @GET("api/notes/{noteId}")
    Call<User> getNoteById(@Path("noteId") String noteId);

    @PUT("/api/notes/{noteId}")
    Call<Void> updateNotes(@Path("noteId") String noteId, @Body HashMap<String, String> map);
}
