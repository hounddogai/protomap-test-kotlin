package com.acme.transport.linehaul.trailer.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalTrailerSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalTrailerSummaryStore()
        val item = newSeasonalTrailerSummary("code-998", "title-446")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
