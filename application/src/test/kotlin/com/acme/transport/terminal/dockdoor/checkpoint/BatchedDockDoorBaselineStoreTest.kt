package com.acme.transport.terminal.dockdoor.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedDockDoorBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedDockDoorBaselineStore()
        val item = newBatchedDockDoorBaseline("code-128", "title-135")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
