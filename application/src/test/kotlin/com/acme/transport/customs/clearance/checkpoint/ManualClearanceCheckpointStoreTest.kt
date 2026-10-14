package com.acme.transport.customs.clearance.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualClearanceCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualClearanceCheckpointStore()
        val item = newManualClearanceCheckpoint("code-381", "name-723")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
