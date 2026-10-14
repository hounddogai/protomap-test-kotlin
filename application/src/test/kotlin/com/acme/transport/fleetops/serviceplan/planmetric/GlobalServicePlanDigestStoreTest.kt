package com.acme.transport.fleetops.serviceplan.planmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalServicePlanDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalServicePlanDigestStore()
        val item = newGlobalServicePlanDigest("code-102", "title-930")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
