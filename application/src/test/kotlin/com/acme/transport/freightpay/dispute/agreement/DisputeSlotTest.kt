package com.acme.transport.freightpay.dispute.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeSlotTest {
    @Test
    fun `starts in new`() {
        val item = newDisputeSlot("code-795", "name-294")
        assertEquals(DisputeSlotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newDisputeSlot("code-795", "name-294")
        try {
            item.moveTo(DisputeSlotStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeSlotStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDisputeSlot("code-795", "name-294")
        item.status = DisputeSlotStatus.FAILED
        assertFails("move") { item.moveTo(DisputeSlotStatus.NEW) }
    }
}
