package com.acme.transport.fleetops.serviceplan.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanQuotaStore()
        val item = newServicePlanQuota("code-548", "label-33")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
