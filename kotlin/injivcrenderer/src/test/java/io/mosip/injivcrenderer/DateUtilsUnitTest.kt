package io.mosip.injivcrenderer

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse

class DateUtilsTest {

    @Test
    fun testFormatDate_ValidDate() {
        val inputDate = "2024-08-05T15:30:00Z"
        val expectedOutput = "2024/08/05"
        val result = DateUtils.formatDate(inputDate)
        assertEquals(expectedOutput, result)
    }


    @Test
    fun testIsValidDateTime_ValidDate() {
        val validDate = "2024-08-05T15:30:00Z"
        assertTrue(DateUtils.isValidDateTime(validDate))
    }

    @Test
    fun testIsValidDateTime_InvalidDate() {
        val invalidDate = "invalid-date"
        assertFalse(DateUtils.isValidDateTime(invalidDate))
    }

    @Test
    fun testIsValidDateTime_EmptyDate() {
        val emptyDate = ""
        assertFalse(DateUtils.isValidDateTime(emptyDate))
    }
}
