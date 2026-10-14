package com.acme.transport.terminal.sortation.sortationbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingSortationCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingSortationCheckpointStore()
        val item = newPendingSortationCheckpoint("code-413", "label-365")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
