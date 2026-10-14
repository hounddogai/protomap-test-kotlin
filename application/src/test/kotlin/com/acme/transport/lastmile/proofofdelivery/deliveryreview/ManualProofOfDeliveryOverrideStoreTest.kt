package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualProofOfDeliveryOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualProofOfDeliveryOverrideStore()
        val item = newManualProofOfDeliveryOverride("code-613", "name-343")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
