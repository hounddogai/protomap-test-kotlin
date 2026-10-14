package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalTrailerYardSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalTrailerYardSummaryStore()
        val item = newSeasonalTrailerYardSummary("code-154", "label-836")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
