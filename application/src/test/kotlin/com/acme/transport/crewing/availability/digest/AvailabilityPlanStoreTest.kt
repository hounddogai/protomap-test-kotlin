package com.acme.transport.crewing.availability.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityPlanStore()
        val item = newAvailabilityPlan("code-134", "label-198")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
