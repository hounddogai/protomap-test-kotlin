package com.acme.transport.fleetops.inspection.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedInspectionCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedInspectionCheckpointStore()
        val item = newCappedInspectionCheckpoint("code-490", "name-391")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
