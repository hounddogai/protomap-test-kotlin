package com.acme.transport.fleetops.serviceplan.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanSlotStore()
        val item = newServicePlanSlot("code-113", "label-770")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
