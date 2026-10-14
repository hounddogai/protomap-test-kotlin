package com.acme.transport.customs.inspection.weightedinspectionplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingInspectionCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingInspectionCheckpointStore()
        val item = newPendingInspectionCheckpoint("code-266", "name-931")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
