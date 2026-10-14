package com.acme.transport.fleetops.serviceplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackServicePlanRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackServicePlanRequestStore()
        val item = newFallbackServicePlanRequest("code-519", "name-490")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
