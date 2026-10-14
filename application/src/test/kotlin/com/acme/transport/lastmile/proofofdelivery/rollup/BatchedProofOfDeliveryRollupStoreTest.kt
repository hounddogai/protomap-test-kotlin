package com.acme.transport.lastmile.proofofdelivery.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedProofOfDeliveryRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedProofOfDeliveryRollupStore()
        val item = newBatchedProofOfDeliveryRollup("code-936", "title-615")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
