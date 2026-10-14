package com.acme.transport.freightpay.dispute.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingDisputeClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingDisputeClaimStore()
        val item = newPendingDisputeClaim("code-413", "name-765")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
