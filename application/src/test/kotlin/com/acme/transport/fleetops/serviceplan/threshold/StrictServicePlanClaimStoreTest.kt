package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictServicePlanClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictServicePlanClaimStore()
        val item = newStrictServicePlanClaim("code-443", "label-584")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
