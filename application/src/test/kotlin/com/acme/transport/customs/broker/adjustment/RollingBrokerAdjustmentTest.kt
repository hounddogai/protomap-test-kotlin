package com.acme.transport.customs.broker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingBrokerAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newRollingBrokerAdjustment("code-16", "name-175")
        assertEquals(RollingBrokerAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRollingBrokerAdjustment("code-16", "name-175")
        try {
            item.moveTo(RollingBrokerAdjustmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingBrokerAdjustmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingBrokerAdjustment("code-16", "name-175")
        item.status = RollingBrokerAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(RollingBrokerAdjustmentStatus.NEW) }
    }
}
