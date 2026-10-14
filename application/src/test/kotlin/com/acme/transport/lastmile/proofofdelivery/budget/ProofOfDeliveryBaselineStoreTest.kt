package com.acme.transport.lastmile.proofofdelivery.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryBaselineStore()
        val item = newProofOfDeliveryBaseline("code-926", "label-78")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
