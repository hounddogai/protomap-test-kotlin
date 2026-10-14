package com.acme.transport.terminal.sortation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationForecastStore()
        val item = newSortationForecast("code-244", "label-524")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
