package com.acme.transport.fleetops.serviceplan.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientServicePlanDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientServicePlanDigestStore()
        val item = newLenientServicePlanDigest("code-195", "title-759")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
