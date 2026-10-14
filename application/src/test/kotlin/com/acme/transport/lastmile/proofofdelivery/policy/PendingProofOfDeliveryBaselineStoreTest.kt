package com.acme.transport.lastmile.proofofdelivery.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingProofOfDeliveryBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingProofOfDeliveryBaselineStore()
        val item = newPendingProofOfDeliveryBaseline("code-608", "title-45")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
