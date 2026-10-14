package com.acme.transport.freightpay.dispute.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeSlotStore()
        val item = newDisputeSlot("code-795", "name-294")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
