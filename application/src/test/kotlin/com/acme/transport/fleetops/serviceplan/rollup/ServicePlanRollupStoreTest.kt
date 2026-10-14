package com.acme.transport.fleetops.serviceplan.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanRollupStore()
        val item = newServicePlanRollup("code-844", "name-821")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
