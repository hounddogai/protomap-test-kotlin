package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingServicePlanRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingServicePlanRollupStore()
        val item = newPendingServicePlanRollup("code-643", "title-572")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
