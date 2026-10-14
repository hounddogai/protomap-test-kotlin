package com.acme.transport.safety.scorecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingScorecardForecastTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingScorecardForecast("code-221", "name-895")
        assertEquals(PendingScorecardForecastStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingScorecardForecast("code-221", "name-895")
        try {
            item.moveTo(PendingScorecardForecastStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingScorecardForecastStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingScorecardForecast("code-221", "name-895")
        item.status = PendingScorecardForecastStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingScorecardForecastStatus.REQUESTED) }
    }
}
