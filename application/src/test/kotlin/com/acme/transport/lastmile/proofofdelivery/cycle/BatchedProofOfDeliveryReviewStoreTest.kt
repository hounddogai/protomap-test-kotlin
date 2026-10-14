package com.acme.transport.lastmile.proofofdelivery.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedProofOfDeliveryReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedProofOfDeliveryReviewStore()
        val item = newBatchedProofOfDeliveryReview("code-875", "name-226")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
