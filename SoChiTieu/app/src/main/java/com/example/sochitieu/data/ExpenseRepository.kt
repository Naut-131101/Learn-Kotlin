package com.example.sochitieu.data

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date

class ExpenseRepository(
    db: FirebaseFirestore = Firebase.firestore,
) {
    private val expenses = db.collection("expenses")

    /** ĐỌC realtime: khoản chi trong một tháng, mới nhất trước. Có thay đổi trên server là Flow phát lại. */
    fun observeMonth(month: YearMonth): Flow<List<Expense>> {
        val zone = ZoneId.systemDefault()
        val start = Timestamp(Date.from(month.atDay(1).atStartOfDay(zone).toInstant()))
        val end = Timestamp(Date.from(month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant()))
        return expenses
            .whereGreaterThanOrEqualTo("spentAt", start)
            .whereLessThan("spentAt", end)
            .orderBy("spentAt", Query.Direction.DESCENDING)
            .snapshots()                                   // Flow<QuerySnapshot>
            .map { snapshot -> snapshot.toObjects(Expense::class.java) }
    }

    /** TẠO: add() tự sinh id cho document mới. */
    suspend fun add(expense: Expense) {
        expenses.add(expense).await()
    }

    /** SỬA: update() chỉ ghi các field được liệt kê — createdAt giữ nguyên. */
    suspend fun update(expense: Expense) {
        expenses.document(expense.id).update(
            mapOf(
                "amount" to expense.amount,
                "category" to expense.category,
                "note" to expense.note,
                "spentAt" to expense.spentAt,
            ),
        ).await()
    }

    /** XOÁ */
    suspend fun delete(id: String) {
        expenses.document(id).delete().await()
    }
}