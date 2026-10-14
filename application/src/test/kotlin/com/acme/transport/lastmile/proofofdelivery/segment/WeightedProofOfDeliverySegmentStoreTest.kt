package com.acme.transport.lastmile.proofofdelivery.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedProofOfDeliverySegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedProofOfDeliverySegmentStore()
        val item = newWeightedProofOfDeliverySegment("code-498", "name-95")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
