package com.example.sochitieu.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

enum class Category(val label: String) {
    FOOD("Ăn uống"),
    TRANSPORT("Đi lại"),
    SHOPPING("Mua sắm"),
    BILLS("Hoá đơn"),
    OTHER("Khác"),
}

/**
 * Một document trong collection "expenses".
 * Mọi thuộc tính đều có giá trị mặc định: Firestore cần constructor không tham số để toObject().
 */
data class Expense(
    @DocumentId val id: String = "",           // id của document, KHÔNG được ghi thành field
    val amount: Long = 0,                      // số tiền VND
    val category: String = Category.OTHER.name, // lưu tên enum, ví dụ "FOOD"
    val note: String = "",
    val spentAt: Timestamp = Timestamp.now(),  // ngày chi — dùng để truy vấn theo tháng
    @ServerTimestamp val createdAt: Timestamp? = null, // null khi gửi → server tự điền giờ server
)