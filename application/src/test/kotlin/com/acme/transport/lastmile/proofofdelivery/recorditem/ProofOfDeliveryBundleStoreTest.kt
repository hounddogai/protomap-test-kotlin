package com.acme.transport.lastmile.proofofdelivery.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryBundleStore()
        val item = newProofOfDeliveryBundle("code-759", "title-712")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
