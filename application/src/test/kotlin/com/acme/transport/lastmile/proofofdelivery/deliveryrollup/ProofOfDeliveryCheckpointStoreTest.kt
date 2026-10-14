package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryCheckpointStore()
        val item = newProofOfDeliveryCheckpoint("code-64", "title-34")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
