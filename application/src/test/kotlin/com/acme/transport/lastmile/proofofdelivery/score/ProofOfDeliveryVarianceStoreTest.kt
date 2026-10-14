package com.acme.transport.lastmile.proofofdelivery.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryVarianceStore()
        val item = newProofOfDeliveryVariance("code-44", "label-551")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
