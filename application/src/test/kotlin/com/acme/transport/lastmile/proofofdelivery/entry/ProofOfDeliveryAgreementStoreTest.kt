package com.acme.transport.lastmile.proofofdelivery.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryAgreementStore()
        val item = newProofOfDeliveryAgreement("code-676", "name-417")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
