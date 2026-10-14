package com.acme.transport.crewing.restbreak.breaksummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedRestBreakSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedRestBreakSummaryStore()
        val item = newBatchedRestBreakSummary("code-850", "name-141")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
