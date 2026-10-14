package com.acme.transport.freightpay.dispute.disputeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyDisputeSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyDisputeSlotStore()
        val item = newLegacyDisputeSlot("code-914", "title-896")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
