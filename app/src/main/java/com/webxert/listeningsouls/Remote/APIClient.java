package com.webxert.listeningsouls.Remote;

import com.webxert.listeningsouls.models.DataMessage;
import com.webxert.listeningsouls.models.NotificationResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIClient {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAZ83mPOI:APA91bFdLCDQWZkA_bgzQExTqlDztKJw8_FwguqqRHCN8WpN2ORcE_gDqIoOck4vsW8b8KSwGRM_HkLnqa114Q3JBHvJ_tCth-MP8PJJ9Nr72_4uM4nLOy0t_XGaRCGBEtuXoowSyfA4"
            })

    @POST("fcm/send")
    Call<NotificationResponse> sendNotification(@Body DataMessage sender);
}
