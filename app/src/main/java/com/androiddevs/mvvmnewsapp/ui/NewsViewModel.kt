package com.androiddevs.mvvmnewsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.mvvmnewsapp.NewsApplication
import com.androiddevs.mvvmnewsapp.models.Article
import com.androiddevs.mvvmnewsapp.models.NewsResponse
import com.androiddevs.mvvmnewsapp.repository.NewsRepository
import com.androiddevs.mvvmnewsapp.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    app : Application,
    val newsRepository: NewsRepository
): AndroidViewModel(app) {

    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var paginateBreakingNews = 1
    var breakingNewsResponse: NewsResponse?  = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var paginateSearchNews = 1
    var searchNewsResponse: NewsResponse?  = null

    init {
        getBreakingNews("us")
    }

    fun getBreakingNews(country: String) = viewModelScope.launch {
       safeBreakingNews(country)
    }

    fun searchNews(querySearchNews: String) = viewModelScope.launch {
        safeSearchNews(querySearchNews)
    }

    fun handlerBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if(response.isSuccessful){
            response.body()?.let { resultResponse ->
                paginateBreakingNews++
                if(breakingNewsResponse == null){
                    breakingNewsResponse = resultResponse
                }else {
                    var oldResponse = breakingNewsResponse?.articles
                    var newResponse = resultResponse.articles
                    oldResponse?.addAll(newResponse)
                }
                return Resource.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun handlerSearchResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if(response.isSuccessful){
            response.body()?.let { resultResponse ->
                paginateSearchNews++
                if(searchNewsResponse == null){
                    searchNewsResponse = resultResponse
                }else {
                    var oldResponse = searchNewsResponse?.articles
                    var newResponse = resultResponse.articles
                    oldResponse?.addAll(newResponse)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun upsert(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getAllArticle() = newsRepository.getAllArticle()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    private suspend fun safeBreakingNews(country: String) {
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(country, paginateBreakingNews)
                breakingNews.postValue(handlerBreakingNewsResponse(response))
            } else {
                breakingNews.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (t: Throwable){
            when (t) {
                is IOException -> breakingNews.postValue(Resource.Error("Network Failure"))
                else -> breakingNews.postValue(Resource.Error("Conversion Error"))

            }
        }
    }

    private suspend fun safeSearchNews(searchQuery: String) {
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getSearchNews(searchQuery, paginateBreakingNews)
                searchNews.postValue(handlerBreakingNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (t: Throwable){
            when (t) {
                is IOException -> searchNews.postValue(Resource.Error("Network Failure"))
                else -> searchNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    fun hasInternetConnection() : Boolean {
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capability = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            when {
                capability.hasTransport(TRANSPORT_WIFI) -> return true
                capability.hasTransport(TRANSPORT_ETHERNET) -> return true
                capability.hasTransport(TRANSPORT_CELLULAR) -> return true
                else -> return false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when(type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }
}