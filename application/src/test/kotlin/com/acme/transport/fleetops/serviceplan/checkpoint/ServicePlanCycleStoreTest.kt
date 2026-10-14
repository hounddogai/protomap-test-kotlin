package com.acme.transport.fleetops.serviceplan.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanCycleStore()
        val item = newServicePlanCycle("code-840", "title-804")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
