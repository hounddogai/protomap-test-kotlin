package com.acme.transport.linehaul.relay.weightedrelayrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRelayForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingRelayForecast("code-127", "label-433")
        assertEquals(PendingRelayForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newPendingRelayForecast("code-127", "label-433")
        try {
            item.moveTo(PendingRelayForecastStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRelayForecastStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingRelayForecast("code-127", "label-433")
        item.status = PendingRelayForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingRelayForecastStatus.PLANNED) }
    }
}
