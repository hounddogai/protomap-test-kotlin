package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalProofOfDeliveryOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalProofOfDeliveryOverrideStore()
        val item = newGlobalProofOfDeliveryOverride("code-291", "name-427")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
