package com.acme.transport.terminal.dockdoor.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedDockDoorWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedDockDoorWindowStore()
        val item = newBatchedDockDoorWindow("code-635", "name-410")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
