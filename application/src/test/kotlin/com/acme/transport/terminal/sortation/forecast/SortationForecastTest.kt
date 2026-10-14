package com.acme.transport.terminal.sortation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationForecastTest {
    @Test
    fun `starts in draft`() {
        val item = newSortationForecast("code-244", "label-524")
        assertEquals(SortationForecastStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newSortationForecast("code-244", "label-524")
        try {
            item.moveTo(SortationForecastStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationForecastStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSortationForecast("code-244", "label-524")
        item.status = SortationForecastStatus.CLOSED
        assertFails("move") { item.moveTo(SortationForecastStatus.DRAFT) }
    }
}
