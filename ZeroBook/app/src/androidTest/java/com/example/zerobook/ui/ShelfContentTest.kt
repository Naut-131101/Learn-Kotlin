package com.example.zerobook.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.zerobook.R
import com.example.zerobook.model.Book
import com.example.zerobook.model.ReadingStatus
import com.example.zerobook.model.ShelfBook
import com.example.zerobook.ui.shelf.ShelfContent
import com.example.zerobook.ui.shelf.ShelfUiState
import com.example.zerobook.ui.theme.ZeroBookTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI test cho ShelfContent (stateless) — không cần ViewModel hay Hilt,
 * chỉ truyền state giả vào và kiểm tra giao diện + callback.
 */
class ShelfContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun emptyShelf_showsEmptyState_andFindBooksButtonTriggersCallback() {
        var findBooksClicked = false
        composeTestRule.setContent {
            ZeroBookTheme(dynamicColor = false) {
                ShelfContent(
                    uiState = ShelfUiState(isLoading = false),
                    onStatusSelected = {},
                    onBookClick = {},
                    onFindBooksClick = { findBooksClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.shelf_empty_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.shelf_empty_action)).performClick()

        assertTrue(findBooksClicked)
    }

    @Test
    fun shelfWithBooks_showsTitle_andClickOpensBook() {
        var clickedBookId: String? = null
        val book = Book(id = "abc", title = "Clean Code", authors = listOf("Robert C. Martin"))
        composeTestRule.setContent {
            ZeroBookTheme(dynamicColor = false) {
                ShelfContent(
                    uiState = ShelfUiState(
                        isLoading = false,
                        selectedStatus = ReadingStatus.READING,
                        books = listOf(ShelfBook(book = book, status = ReadingStatus.READING)),
                        counts = mapOf(ReadingStatus.READING to 1),
                    ),
                    onStatusSelected = {},
                    onBookClick = { clickedBookId = it },
                    onFindBooksClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Clean Code").assertIsDisplayed().performClick()

        assertEquals("abc", clickedBookId)
    }
}
