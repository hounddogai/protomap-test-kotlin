package com.acme.transport.lastmile.proofofdelivery.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryDigestStore()
        val item = newProofOfDeliveryDigest("code-957", "label-82")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
