package com.acme.transport.fleetops.serviceplan.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledServicePlanTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledServicePlanTierStore()
        val item = newScheduledServicePlanTier("code-520", "title-174")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
