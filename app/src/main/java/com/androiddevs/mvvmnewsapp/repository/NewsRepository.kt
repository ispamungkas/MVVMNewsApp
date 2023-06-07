package com.androiddevs.mvvmnewsapp.repository

import androidx.lifecycle.LiveData
import com.androiddevs.mvvmnewsapp.api.RetrofitInstance
import com.androiddevs.mvvmnewsapp.db.DatabaseArticle
import com.androiddevs.mvvmnewsapp.models.Article
import com.androiddevs.mvvmnewsapp.models.NewsResponse
import retrofit2.Response

class NewsRepository(
    val db: DatabaseArticle
) {
    suspend fun getBreakingNews(country: String, pageNumber: Int): Response<NewsResponse> {
        return RetrofitInstance.api.getBreakingNews(country, pageNumber)
    }

    suspend fun getSearchNews(searchQuery: String, pageNumber: Int): Response<NewsResponse> {
        return RetrofitInstance.api.searchForNews(searchQuery, pageNumber)
    }

    suspend fun upsert(article: Article): Long {
        return db.getArticleDao().upInstert(article)
    }

    fun getAllArticle(): LiveData<List<Article>>{
        return db.getArticleDao().getAllArticle();
    }

    suspend fun deleteArticle(article: Article){
        return db.getArticleDao().deleteArticle(article)
    }
}