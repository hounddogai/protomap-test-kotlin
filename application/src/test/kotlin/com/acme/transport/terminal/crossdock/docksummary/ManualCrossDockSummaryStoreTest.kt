package com.acme.transport.terminal.crossdock.docksummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualCrossDockSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualCrossDockSummaryStore()
        val item = newManualCrossDockSummary("code-244", "name-364")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
