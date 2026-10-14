package com.acme.transport.terminal.crossdock.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyCrossDockSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyCrossDockSummaryStore()
        val item = newLegacyCrossDockSummary("code-883", "title-264")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
