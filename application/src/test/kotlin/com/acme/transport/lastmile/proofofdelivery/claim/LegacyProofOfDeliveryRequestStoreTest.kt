package com.acme.transport.lastmile.proofofdelivery.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyProofOfDeliveryRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyProofOfDeliveryRequestStore()
        val item = newLegacyProofOfDeliveryRequest("code-489", "title-869")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
