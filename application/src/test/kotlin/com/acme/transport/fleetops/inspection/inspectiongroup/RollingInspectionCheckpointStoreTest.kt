package com.acme.transport.fleetops.inspection.inspectiongroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingInspectionCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingInspectionCheckpointStore()
        val item = newRollingInspectionCheckpoint("code-656", "name-299")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
