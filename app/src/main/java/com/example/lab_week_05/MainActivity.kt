package com.example.lab_week_05

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.lab_week_05.model.ImageData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private val catApiService by lazy {
        retrofit.create(CatApiService::class.java)
    }

    private lateinit var apiResponseView: TextView
    private lateinit var imageResultView: ImageView

    private val imageLoader: ImageLoader by lazy {
        GlideLoader(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiResponseView = findViewById(R.id.api_response)
        imageResultView = findViewById(R.id.image_result)

        getCatImageResponse()
    }

    private fun getCatImageResponse() {
        val call = catApiService.searchImages(1, "full")
        call.enqueue(object : Callback<List<ImageData>> {
            override fun onResponse(
                call: Call<List<ImageData>>,
                response: Response<List<ImageData>>
            ) {
                if (response.isSuccessful) {
                    val firstItem = response.body()?.firstOrNull()

                    runOnUiThread {
                        if (firstItem == null) {
                            apiResponseView.text = "No data"
                            imageResultView.setImageDrawable(null)
                            return@runOnUiThread
                        }

                        val breedName = firstItem.breeds?.firstOrNull()?.name ?: "Unknown"
                        val imageUrl = firstItem.imageUrl.orEmpty()

                        apiResponseView.text = "Breed: $breedName"

                        if (imageUrl.isNotBlank()) {
                            imageLoader.loadImage(imageUrl, imageResultView)
                        } else {
                            Log.d(TAG, "Missing image URL for item")
                            imageResultView.setImageDrawable(null)
                        }
                    }
                } else {
                    Log.e(TAG, "API error: ${response.code()} ${response.message()}")
                    runOnUiThread {
                        apiResponseView.text = "Error: ${response.code()}"
                    }
                }
            }

            override fun onFailure(call: Call<List<ImageData>>, t: Throwable) {
                Log.e(TAG, "Network error", t)
                runOnUiThread {
                    apiResponseView.text = "Failed: ${t.message}"
                }
            }
        })
    }
}
