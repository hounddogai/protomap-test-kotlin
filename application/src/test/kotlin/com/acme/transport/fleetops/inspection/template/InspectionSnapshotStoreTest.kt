package com.acme.transport.fleetops.inspection.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionSnapshotStore()
        val item = newInspectionSnapshot("code-352", "label-302")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
