package com.acme.transport.freightpay.dispute.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredDisputeSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredDisputeSlotStore()
        val item = newTieredDisputeSlot("code-155", "label-308")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
