package com.example.quanlydiemsv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quanlydiemsv.ui.theme.QuanLyDiemSVTheme
import kotlin.math.round

// Data class đại diện cho Sinh viên
data class Student(
    val id: String,
    val name: String,
    val processScore: Double,
    val midtermScore: Double,
    val finalScore: Double
) {
    val averageScore: Double
        get() {
            val raw = processScore * 0.2 + midtermScore * 0.3 + finalScore * 0.5
            return round(raw * 10) / 10.0
        }

    val classification: String
        get() = when {
            averageScore >= 8.5 -> "Giỏi"
            averageScore >= 7.0 -> "Khá"
            averageScore >= 5.0 -> "Trung bình"
            else -> "Yếu"
        }
}

fun String.toTitleCase(): String =
    trim()
        .split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .joinToString(" ") { word -> word.lowercase().replaceFirstChar { it.uppercase() } }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuanLyDiemSVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val studentList = remember { mutableStateListOf<Student>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản Lý Điểm Sinh Viên", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Danh sách") },
                    label = { Text("Danh sách") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Thêm SV") },
                    label = { Text("Thêm SV") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Thống kê") },
                    label = { Text("Thống kê") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> StudentListScreen(students = studentList, onDeleteStudent = { studentList.remove(it) })
                1 -> AddStudentScreen(
                    students = studentList,
                    onStudentAdded = { student ->
                        studentList.add(student)
                        selectedTab = 0
                    }
                )
                2 -> StatisticsScreen(students = studentList)
            }
        }
    }
}

@Composable
fun StudentListScreen(students: List<Student>, onDeleteStudent: (Student) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(searchQuery, students) {
        if (searchQuery.isBlank()) students
        else students.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.id.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Tìm kiếm theo tên hoặc mã SV") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Xóa tìm kiếm")
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (students.isEmpty()) "Danh sách sinh viên trống.\nHãy chuyển sang tab \"Thêm SV\" để thêm sinh viên mới."
                    else "Không tìm thấy sinh viên nào khớp với \"$searchQuery\".",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList, key = { it.id }) { student ->
                    StudentItemCard(student = student, onDelete = { onDeleteStudent(student) })
                }
            }
        }
    }
}

@Composable
fun StudentItemCard(student: Student, onDelete: () -> Unit) {
    val badgeColor = when (student.classification) {
        "Giỏi" -> Color(0xFF2E7D32)
        "Khá" -> Color(0xFF1565C0)
        "Trung bình" -> Color(0xFFEF6C00)
        else -> Color(0xFFC62828)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mã SV: ${student.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = badgeColor,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = student.classification,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa sinh viên",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "QT: ${student.processScore}", style = MaterialTheme.typography.bodySmall)
                Text(text = "GK: ${student.midtermScore}", style = MaterialTheme.typography.bodySmall)
                Text(text = "CK: ${student.finalScore}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "TB: ${student.averageScore}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AddStudentScreen(students: List<Student>, onStudentAdded: (Student) -> Unit) {
    var id by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var processScoreText by remember { mutableStateOf("") }
    var midtermScoreText by remember { mutableStateOf("") }
    var finalScoreText by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Thêm Sinh Viên Mới",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = id,
            onValueChange = { id = it },
            label = { Text("Mã SV") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Họ tên") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = processScoreText,
            onValueChange = { processScoreText = it },
            label = { Text("Điểm quá trình (0 - 10)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        OutlinedTextField(
            value = midtermScoreText,
            onValueChange = { midtermScoreText = it },
            label = { Text("Điểm giữa kỳ (0 - 10)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        OutlinedTextField(
            value = finalScoreText,
            onValueChange = { finalScoreText = it },
            label = { Text("Điểm cuối kỳ (0 - 10)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val cleanId = id.trim().uppercase()
                val cleanName = name.toTitleCase()
                val p = processScoreText.trim().replace(',', '.').toDoubleOrNull()
                val m = midtermScoreText.trim().replace(',', '.').toDoubleOrNull()
                val f = finalScoreText.trim().replace(',', '.').toDoubleOrNull()

                when {
                    cleanId.isEmpty() -> errorMessage = "Mã sinh viên không được để trống."
                    students.any { it.id.equals(cleanId, ignoreCase = true) } -> errorMessage = "Mã sinh viên $cleanId đã tồn tại."
                    cleanName.isEmpty() -> errorMessage = "Họ tên không được để trống."
                    p == null || p !in 0.0..10.0 -> errorMessage = "Điểm quá trình phải từ 0 đến 10."
                    m == null || m !in 0.0..10.0 -> errorMessage = "Điểm giữa kỳ phải từ 0 đến 10."
                    f == null || f !in 0.0..10.0 -> errorMessage = "Điểm cuối kỳ phải từ 0 đến 10."
                    else -> {
                        errorMessage = null
                        onStudentAdded(
                            Student(
                                id = cleanId,
                                name = cleanName,
                                processScore = p,
                                midtermScore = m,
                                finalScore = f
                            )
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Thêm sinh viên")
        }
    }
}

@Composable
fun StatisticsScreen(students: List<Student>) {
    if (students.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chưa có dữ liệu sinh viên để thống kê.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val classSize = students.size
    val averageClassScore = students.map { it.averageScore }.average()
    val maxStudent = students.maxByOrNull { it.averageScore }
    val minStudent = students.minByOrNull { it.averageScore }

    val excellentCount = students.count { it.classification == "Giỏi" }
    val goodCount = students.count { it.classification == "Khá" }
    val averageCount = students.count { it.classification == "Trung bình" }
    val weakCount = students.count { it.classification == "Yếu" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Thống Kê Lớp Học",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Sĩ số: $classSize sinh viên",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Điểm TB lớp: ${"%.2f".format(averageClassScore)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (maxStudent != null && minStudent != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Điểm TB cao nhất:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${maxStudent.name} (${maxStudent.id}) — ${maxStudent.averageScore}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Điểm TB thấp nhất:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${minStudent.name} (${minStudent.id}) — ${minStudent.averageScore}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Xếp loại học tập:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatRow(label = "Giỏi", count = excellentCount, total = classSize, color = Color(0xFF2E7D32))
                StatRow(label = "Khá", count = goodCount, total = classSize, color = Color(0xFF1565C0))
                StatRow(label = "Trung bình", count = averageCount, total = classSize, color = Color(0xFFEF6C00))
                StatRow(label = "Yếu", count = weakCount, total = classSize, color = Color(0xFFC62828))
            }
        }
    }
}

@Composable
fun StatRow(label: String, count: Int, total: Int, color: Color) {
    val percentage = if (total > 0) (count.toFloat() / total) * 100 else 0f
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label: $count", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            text = "${"%.1f".format(percentage)}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
