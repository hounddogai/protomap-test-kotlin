package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualProofOfDeliveryClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualProofOfDeliveryClaimStore()
        val item = newManualProofOfDeliveryClaim("code-168", "label-549")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
