package com.acme.transport.fleetops.serviceplan.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanVarianceStore()
        val item = newServicePlanVariance("code-947", "label-975")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
