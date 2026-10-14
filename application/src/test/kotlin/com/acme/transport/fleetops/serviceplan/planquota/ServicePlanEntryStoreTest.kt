package com.acme.transport.fleetops.serviceplan.planquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanEntryStore()
        val item = newServicePlanEntry("code-805", "title-193")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
