package com.acme.transport.fleetops.serviceplan.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanSnapshotStore()
        val item = newServicePlanSnapshot("code-254", "label-406")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
