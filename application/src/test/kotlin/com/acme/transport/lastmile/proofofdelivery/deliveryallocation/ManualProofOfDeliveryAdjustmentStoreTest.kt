package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualProofOfDeliveryAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualProofOfDeliveryAdjustmentStore()
        val item = newManualProofOfDeliveryAdjustment("code-255", "title-998")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
