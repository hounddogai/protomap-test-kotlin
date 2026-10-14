package com.acme.transport.lastmile.proofofdelivery.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryPolicyStore()
        val item = newProofOfDeliveryPolicy("code-142", "name-173")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
