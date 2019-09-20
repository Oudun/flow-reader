package com.veve.flowreader.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;


@Dao
public interface DaoAccess {

    @Query("SELECT * FROM BookRecord WHERE url = :url")
    BookRecord fetchBook(String url);

    @Query("SELECT * FROM BookRecord")
    List<BookRecord> listBooks();

    @Query("DELETE FROM BookRecord WHERE id = :bookId")
    void deleteBook(long bookId);

    @Insert
    long addBook(BookRecord bookRecord);

    @Update
    void updateBook(BookRecord bookRecord);

    @Query("SELECT * FROM BookRecord WHERE id = :bookId")
    BookRecord getBook(Long bookId);

//    @Query("SELECT * FROM PageGlyphRecord WHERE bookId = :bookId AND position = :position")
//    List<PageGlyphRecord> getPageGlyphs(Long bookId, Integer position);

    @Query("SELECT * FROM PageGlyphRecord WHERE bookId = :bookId AND position = :position")
    List<PageGlyphRecord> getPageGlyphs(Long bookId, Integer position);

    @Query("SELECT * FROM PageGlyphRecord")
    List<PageGlyphRecord> getPageGlyphs();

    @Insert
    void insertGlyphs(List<PageGlyphRecord> glyphs);

    @Query("DELETE FROM PageGlyphRecord WHERE bookId = :bookId")
    void deleteBookGlyphs(long bookId);

}