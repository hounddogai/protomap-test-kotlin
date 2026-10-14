package com.acme.transport.crewing.availability.availabilityscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryAvailabilityPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryAvailabilityPlanStore()
        val item = newPrimaryAvailabilityPlan("code-614", "name-238")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
