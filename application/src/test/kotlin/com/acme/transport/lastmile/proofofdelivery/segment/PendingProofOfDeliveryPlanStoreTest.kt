package com.acme.transport.lastmile.proofofdelivery.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingProofOfDeliveryPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingProofOfDeliveryPlanStore()
        val item = newPendingProofOfDeliveryPlan("code-608", "title-993")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
