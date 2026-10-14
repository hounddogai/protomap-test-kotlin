package com.acme.transport.lastmile.proofofdelivery.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryProposalStore()
        val item = newProofOfDeliveryProposal("code-972", "title-715")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
