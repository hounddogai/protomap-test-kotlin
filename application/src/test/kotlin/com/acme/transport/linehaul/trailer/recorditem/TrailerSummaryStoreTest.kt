package com.acme.transport.linehaul.trailer.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerSummaryStore()
        val item = newTrailerSummary("code-842", "name-28")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
