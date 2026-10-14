package com.acme.transport.fleetops.serviceplan.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedServicePlanClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedServicePlanClaimStore()
        val item = newCappedServicePlanClaim("code-615", "title-743")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
