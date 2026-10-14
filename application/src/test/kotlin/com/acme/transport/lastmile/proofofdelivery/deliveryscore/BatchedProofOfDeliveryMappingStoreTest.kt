package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedProofOfDeliveryMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedProofOfDeliveryMappingStore()
        val item = newBatchedProofOfDeliveryMapping("code-697", "name-231")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
