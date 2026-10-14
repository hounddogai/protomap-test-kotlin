package com.acme.transport.fleetops.leasereturn.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedLeaseReturnProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedLeaseReturnProfileStore()
        val item = newWeightedLeaseReturnProfile("code-800", "label-821")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
