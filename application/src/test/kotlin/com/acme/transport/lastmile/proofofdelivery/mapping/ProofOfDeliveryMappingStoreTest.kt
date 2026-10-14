package com.acme.transport.lastmile.proofofdelivery.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryMappingStore()
        val item = newProofOfDeliveryMapping("code-751", "name-893")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
