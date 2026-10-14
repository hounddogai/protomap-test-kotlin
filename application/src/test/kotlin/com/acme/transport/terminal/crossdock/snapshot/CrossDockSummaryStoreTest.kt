package com.acme.transport.terminal.crossdock.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockSummaryStore()
        val item = newCrossDockSummary("code-686", "label-499")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
