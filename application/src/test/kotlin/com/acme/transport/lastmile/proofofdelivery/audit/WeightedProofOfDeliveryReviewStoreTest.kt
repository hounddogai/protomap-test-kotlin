package com.acme.transport.lastmile.proofofdelivery.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedProofOfDeliveryReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedProofOfDeliveryReviewStore()
        val item = newWeightedProofOfDeliveryReview("code-571", "label-512")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
