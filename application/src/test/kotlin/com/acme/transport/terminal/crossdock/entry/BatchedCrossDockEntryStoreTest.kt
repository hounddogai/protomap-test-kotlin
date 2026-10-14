package com.acme.transport.terminal.crossdock.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedCrossDockEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedCrossDockEntryStore()
        val item = newBatchedCrossDockEntry("code-923", "label-465")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
