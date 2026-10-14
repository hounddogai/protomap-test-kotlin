package com.acme.transport.freightpay.dispute.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedDisputeClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedDisputeClaimStore()
        val item = newCappedDisputeClaim("code-183", "label-409")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
