package com.acme.transport.fleetops.inspection.inspectiondraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingInspectionRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingInspectionRequestStore()
        val item = newPendingInspectionRequest("code-964", "name-711")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
