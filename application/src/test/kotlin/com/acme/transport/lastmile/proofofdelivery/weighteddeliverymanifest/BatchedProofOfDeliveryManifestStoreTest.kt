package com.acme.transport.lastmile.proofofdelivery.weighteddeliverymanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedProofOfDeliveryManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedProofOfDeliveryManifestStore()
        val item = newBatchedProofOfDeliveryManifest("code-895", "title-873")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
