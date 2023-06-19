package com.example.storyapp.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.storyapp.api.ApiService
import com.example.storyapp.preferences.UserPreference
import com.example.storyapp.response_model.GetStoriesResponse
import com.example.storyapp.response_model.ListStoryItem
import kotlinx.coroutines.flow.first
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class StoryPagingSource(private val apiService: ApiService, private val context: Context) :
    PagingSource<Int, ListStoryItem>() {

    companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    private lateinit var userPreference: UserPreference

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {

        userPreference = UserPreference.getInstance(context.dataStore)

        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val loginResult = userPreference.getUser().first()
            Log.d("LoginResult", loginResult.token)

            val listStory = suspendCoroutine { continuation ->
                val responseData =
                    apiService.getStories("Bearer ${loginResult.token}", position, params.loadSize)
                responseData.enqueue(object : Callback<GetStoriesResponse> {
                    override fun onResponse(
                        call: Call<GetStoriesResponse>,
                        response: Response<GetStoriesResponse>
                    ) {
                        val stories = response.body()?.listStory as List<ListStoryItem>
                        Log.d("TES RESPONSE", "onResponse: ${response.body()}")
                        stories[0].name?.let { Log.d("TES LIST", it) }
                        continuation.resume(stories)
                    }

                    override fun onFailure(call: Call<GetStoriesResponse>, t: Throwable) {
                        continuation.resumeWithException(t)
                    }
                })
            }

            LoadResult.Page(
                data = listStory,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (listStory.isEmpty()) null else position + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}