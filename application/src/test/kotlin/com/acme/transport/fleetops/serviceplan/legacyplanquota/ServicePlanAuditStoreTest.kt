package com.acme.transport.fleetops.serviceplan.legacyplanquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanAuditStore()
        val item = newServicePlanAudit("code-509", "name-829")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
