package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalProofOfDeliveryAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalProofOfDeliveryAllocationStore()
        val item = newSeasonalProofOfDeliveryAllocation("code-6", "label-31")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
