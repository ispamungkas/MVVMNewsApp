package com.androiddevs.mvvmnewsapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androiddevs.mvvmnewsapp.models.Article

@Dao
interface ArticleDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upInstert(article : Article) : Long

    @Query("SELECT * FROM articles")
    fun getAllArticle() : LiveData<List<Article>>

    @Delete()
    suspend fun deleteArticle(article : Article)

}