package com.acme.transport.lastmile.proofofdelivery.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryBatchStore()
        val item = newProofOfDeliveryBatch("code-741", "label-367")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
