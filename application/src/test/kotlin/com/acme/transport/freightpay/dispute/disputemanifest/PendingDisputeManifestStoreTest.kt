package com.acme.transport.freightpay.dispute.disputemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingDisputeManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingDisputeManifestStore()
        val item = newPendingDisputeManifest("code-725", "label-991")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
