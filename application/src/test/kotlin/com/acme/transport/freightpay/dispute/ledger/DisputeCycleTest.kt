package com.acme.transport.freightpay.dispute.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newDisputeCycle("code-516", "label-87")
        assertEquals(DisputeCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newDisputeCycle("code-516", "label-87")
        try {
            item.moveTo(DisputeCycleStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeCycleStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDisputeCycle("code-516", "label-87")
        item.status = DisputeCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(DisputeCycleStatus.PLANNED) }
    }
}
