package com.acme.transport.freightpay.dispute.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeClaimStore()
        val item = newDisputeClaim("code-251", "label-389")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
