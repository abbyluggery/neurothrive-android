package com.neurothrive.assistant.data.local.dao

import androidx.room.*
import com.neurothrive.assistant.data.local.entities.Coupon
import kotlinx.coroutines.flow.Flow

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupons WHERE isActive = 1 AND expirationDate >= :currentTime ORDER BY expirationDate ASC")
    fun getActive(currentTime: Long = System.currentTimeMillis()): Flow<List<Coupon>>

    @Query("SELECT * FROM coupons WHERE id = :id")
    suspend fun getById(id: String): Coupon?

    @Query("SELECT * FROM coupons WHERE salesforceId = :salesforceId")
    suspend fun getBySalesforceId(salesforceId: String): Coupon?

    @Query("SELECT * FROM coupons WHERE itemName LIKE '%' || :itemName || '%' AND isActive = 1 AND expirationDate >= :currentTime")
    suspend fun findMatchingCoupons(itemName: String, currentTime: Long = System.currentTimeMillis()): List<Coupon>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(coupon: Coupon)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(coupons: List<Coupon>)

    @Update
    suspend fun update(coupon: Coupon)

    @Delete
    suspend fun delete(coupon: Coupon)

    @Query("DELETE FROM coupons")
    suspend fun deleteAll()

    @Query("UPDATE coupons SET isActive = 0 WHERE expirationDate < :currentTime")
    suspend fun deactivateExpired(currentTime: Long = System.currentTimeMillis())
}
