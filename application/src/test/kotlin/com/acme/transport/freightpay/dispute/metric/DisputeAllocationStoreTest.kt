package com.acme.transport.freightpay.dispute.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeAllocationStore()
        val item = newDisputeAllocation("code-977", "name-794")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
