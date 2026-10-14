package com.acme.transport.fleetops.serviceplan.planmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanWindowStore()
        val item = newServicePlanWindow("code-965", "name-755")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
