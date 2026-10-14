package com.acme.transport.network.transittime.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedTransitTimeEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedTransitTimeEntryStore()
        val item = newBatchedTransitTimeEntry("code-724", "title-847")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
