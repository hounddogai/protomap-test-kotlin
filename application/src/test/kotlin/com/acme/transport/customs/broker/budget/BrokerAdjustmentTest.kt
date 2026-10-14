package com.acme.transport.customs.broker.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerAdjustmentTest {
    @Test
    fun `starts in planned`() {
        val item = newBrokerAdjustment("code-82", "title-533")
        assertEquals(BrokerAdjustmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newBrokerAdjustment("code-82", "title-533")
        try {
            item.moveTo(BrokerAdjustmentStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerAdjustmentStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBrokerAdjustment("code-82", "title-533")
        item.status = BrokerAdjustmentStatus.CANCELLED
        assertFails("move") { item.moveTo(BrokerAdjustmentStatus.PLANNED) }
    }
}
