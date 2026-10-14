package com.acme.transport.customs.broker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledBrokerScoreTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledBrokerScore("code-541", "label-300")
        assertEquals(ScheduledBrokerScoreStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newScheduledBrokerScore("code-541", "label-300")
        try {
            item.moveTo(ScheduledBrokerScoreStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledBrokerScoreStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledBrokerScore("code-541", "label-300")
        item.status = ScheduledBrokerScoreStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledBrokerScoreStatus.IDLE) }
    }
}
