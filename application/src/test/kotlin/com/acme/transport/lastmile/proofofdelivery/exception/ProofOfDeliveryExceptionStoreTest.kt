package com.acme.transport.lastmile.proofofdelivery.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryExceptionStore()
        val item = newProofOfDeliveryException("code-224", "name-844")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
