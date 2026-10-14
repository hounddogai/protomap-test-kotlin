package com.acme.transport.freightpay.dispute.disputeforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingDisputeDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingDisputeDigestStore()
        val item = newPendingDisputeDigest("code-8", "title-503")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
