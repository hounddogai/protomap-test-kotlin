package com.acme.transport.lastmile.proofofdelivery.deliverypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingProofOfDeliveryDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingProofOfDeliveryDigestStore()
        val item = newPendingProofOfDeliveryDigest("code-48", "label-212")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
