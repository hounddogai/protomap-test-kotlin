package com.acme.transport.fleetops.serviceplan.planledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyServicePlanBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyServicePlanBaselineStore()
        val item = newLegacyServicePlanBaseline("code-548", "name-333")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
