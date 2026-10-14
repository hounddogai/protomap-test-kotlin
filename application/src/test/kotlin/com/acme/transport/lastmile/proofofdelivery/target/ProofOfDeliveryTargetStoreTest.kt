package com.acme.transport.lastmile.proofofdelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryTargetStore()
        val item = newProofOfDeliveryTarget("code-812", "label-421")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
