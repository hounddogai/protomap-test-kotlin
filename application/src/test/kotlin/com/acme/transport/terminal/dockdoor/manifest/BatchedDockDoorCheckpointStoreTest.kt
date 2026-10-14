package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedDockDoorCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedDockDoorCheckpointStore()
        val item = newBatchedDockDoorCheckpoint("code-59", "title-176")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
