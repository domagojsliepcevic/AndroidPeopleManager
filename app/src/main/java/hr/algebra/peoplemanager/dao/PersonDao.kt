package hr.algebra.peoplemanager.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PersonDao {

    @Query("select * from people")
    fun getPeople(): MutableList<Person>

    @Query("select * from people where _id =:id")
    fun getPerson(id: Long): Person?

    @Insert
    fun insert(person: Person)
    @Update
    fun update(person: Person)
    @Delete
    fun delete(person: Person)

}