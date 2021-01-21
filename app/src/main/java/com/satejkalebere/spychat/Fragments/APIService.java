package com.satejkalebere.spychat.Fragments;

import com.satejkalebere.spychat.Notifications.MyResponse;
import com.satejkalebere.spychat.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=your_API_key"
            }


    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
