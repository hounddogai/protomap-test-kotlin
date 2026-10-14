package com.acme.transport.linehaul.tender.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderSummaryStore()
        val item = newTenderSummary("code-499", "title-471")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
