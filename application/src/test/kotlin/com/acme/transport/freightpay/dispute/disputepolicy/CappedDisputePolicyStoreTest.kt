package com.acme.transport.freightpay.dispute.disputepolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedDisputePolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedDisputePolicyStore()
        val item = newCappedDisputePolicy("code-834", "title-45")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
