package com.acme.transport.freightpay.dispute.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingDisputeBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingDisputeBaselineStore()
        val item = newPendingDisputeBaseline("code-687", "label-916")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
