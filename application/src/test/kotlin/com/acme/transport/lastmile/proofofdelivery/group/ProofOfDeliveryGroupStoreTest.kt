package com.acme.transport.lastmile.proofofdelivery.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryGroupStore()
        val item = newProofOfDeliveryGroup("code-540", "name-69")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
