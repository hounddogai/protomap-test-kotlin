package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliverySegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliverySegmentStore()
        val item = newProofOfDeliverySegment("code-105", "name-150")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
