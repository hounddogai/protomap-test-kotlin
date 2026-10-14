package com.acme.transport.lastmile.proofofdelivery.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryRuleStore()
        val item = newProofOfDeliveryRule("code-957", "label-960")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
