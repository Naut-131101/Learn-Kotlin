package com.example.quanlydiemsv

// Bước 1 — Khai báo dữ liệu
val ids = mutableListOf<String>()
val names = mutableListOf<String>()
val processScores = mutableListOf<Double>()
val midtermScores = mutableListOf<Double>()
val finalScores = mutableListOf<Double>()

// Bước 2 — Các hàm tính toán thuần
fun average(process: Double, midterm: Double, final: Double): Double {
    val raw = process * 0.2 + midterm * 0.3 + final * 0.5
    return Math.round(raw * 10) / 10.0
}

fun classify(avg: Double): String = when {
    avg >= 8.5 -> "Giỏi"
    avg >= 7.0 -> "Khá"
    avg >= 5.0 -> "Trung bình"
    else -> "Yếu"
}

fun String.toTitleCase(): String =
    trim()
        .split(Regex("\\s+"))
        .joinToString(" ") { word -> word.lowercase().replaceFirstChar { it.uppercase() } }

// Bước 3 — Các hàm nhập liệu an toàn
fun readNonBlank(label: String): String {
    while (true) {
        print("$label: ")
        val value = readlnOrNull()?.trim().orEmpty()
        if (value.isNotEmpty()) return value
        println("  → Không được để trống.")
    }
}

fun readScore(label: String): Double {
    while (true) {
        print("$label (0-10): ")
        val value = readlnOrNull()?.trim()?.replace(',', '.')?.toDoubleOrNull()
        if (value != null && value in 0.0..10.0) return value
        println("  → Điểm phải là số từ 0 đến 10.")
    }
}

// Bước 4 — Thêm sinh viên
fun addStudent() {
    val id = readNonBlank("Mã SV").uppercase()
    if (id in ids) {
        println("  → Mã $id đã tồn tại.")
        return
    }
    val name = readNonBlank("Họ tên").toTitleCase()
    val p = readScore("Điểm quá trình")
    val m = readScore("Điểm giữa kỳ")
    val f = readScore("Điểm cuối kỳ")

    ids.add(id)
    names.add(name)
    processScores.add(p)
    midtermScores.add(m)
    finalScores.add(f)

    val avg = average(p, m, f)
    println("→ Đã thêm $name (TB $avg — ${classify(avg)})")
}

// Bước 5 — In bảng điểm
fun printHeader() {
    println(
        "STT".padEnd(5) + "Mã SV".padEnd(8) + "Họ tên".padEnd(24) +
                "QT".padEnd(5) + "GK".padEnd(5) + "CK".padEnd(5) + "TB".padEnd(5) + "Xếp loại"
    )
}

fun printRow(stt: Int, i: Int) {
    val avg = average(processScores[i], midtermScores[i], finalScores[i])
    println(
        "$stt".padEnd(5) + ids[i].padEnd(8) + names[i].padEnd(24) +
                "${processScores[i]}".padEnd(5) + "${midtermScores[i]}".padEnd(5) +
                "${finalScores[i]}".padEnd(5) + "$avg".padEnd(5) + classify(avg)
    )
}

fun printAll() {
    if (ids.isEmpty()) {
        println("Danh sách trống.")
        return
    }
    printHeader()
    for (i in ids.indices) printRow(i + 1, i)
}

// Bước 6 — Tìm kiếm
fun searchByName() {
    val keyword = readNonBlank("Nhập tên cần tìm")
    var found = 0
    for (i in names.indices) {
        if (names[i].contains(keyword, ignoreCase = true)) {
            if (found == 0) printHeader()
            found++
            printRow(found, i)
        }
    }
    if (found == 0) println("Không tìm thấy sinh viên nào khớp \"$keyword\".")
}

// Bước 7 — Thống kê
fun printStatistics() {
    if (ids.isEmpty()) {
        println("Chưa có dữ liệu để thống kê.")
        return
    }
    // Tính TB của từng sinh viên một lần, lưu vào danh sách để dùng lại
    val avgs = mutableListOf<Double>()
    for (i in ids.indices) {
        avgs.add(average(processScores[i], midtermScores[i], finalScores[i]))
    }

    var sum = 0.0
    var maxIndex = 0
    var minIndex = 0
    var countExcellent = 0
    var countGood = 0
    var countAverage = 0
    var countWeak = 0

    for (i in avgs.indices) {
        sum += avgs[i]
        if (avgs[i] > avgs[maxIndex]) maxIndex = i
        if (avgs[i] < avgs[minIndex]) minIndex = i
        when (classify(avgs[i])) {
            "Giỏi" -> countExcellent++
            "Khá" -> countGood++
            "Trung bình" -> countAverage++
            else -> countWeak++
        }
    }
    println("Sĩ số: ${ids.size}")
    println("Điểm TB lớp: ${"%.2f".format(sum / ids.size)}")
    println("Cao nhất: ${names[maxIndex]} (${avgs[maxIndex]})")
    println("Thấp nhất: ${names[minIndex]} (${avgs[minIndex]})")
    println("Giỏi: $countExcellent | Khá: $countGood | Trung bình: $countAverage | Yếu: $countWeak")
}

// Bước 8 — Menu chính
fun main() {
    val menu = """
        ===== QUẢN LÝ ĐIỂM =====
        1. Thêm sinh viên
        2. Xem bảng điểm
        3. Tìm theo tên
        4. Thống kê lớp
        0. Thoát
    """.trimIndent()

    while (true) {
        println()
        println(menu)
        print("Chọn: ")
        when (readlnOrNull()?.trim()) {
            "1" -> addStudent()
            "2" -> printAll()
            "3" -> searchByName()
            "4" -> printStatistics()
            "0", null -> {
                println("Tạm biệt!")
                return
            }

            else -> println("Lựa chọn không hợp lệ.")
        }
    }
}