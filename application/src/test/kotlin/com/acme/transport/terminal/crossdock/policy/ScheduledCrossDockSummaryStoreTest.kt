package com.acme.transport.terminal.crossdock.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledCrossDockSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledCrossDockSummaryStore()
        val item = newScheduledCrossDockSummary("code-262", "label-527")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
