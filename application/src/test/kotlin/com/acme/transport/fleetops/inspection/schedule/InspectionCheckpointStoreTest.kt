package com.acme.transport.fleetops.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionCheckpointStore()
        val item = newInspectionCheckpoint("code-887", "title-450")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
