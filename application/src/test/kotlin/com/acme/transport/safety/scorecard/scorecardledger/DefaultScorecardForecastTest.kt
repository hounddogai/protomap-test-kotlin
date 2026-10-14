package com.acme.transport.safety.scorecard.scorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultScorecardForecastTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultScorecardForecast("code-786", "name-524")
        assertEquals(DefaultScorecardForecastStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDefaultScorecardForecast("code-786", "name-524")
        try {
            item.moveTo(DefaultScorecardForecastStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultScorecardForecastStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultScorecardForecast("code-786", "name-524")
        item.status = DefaultScorecardForecastStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultScorecardForecastStatus.IDLE) }
    }
}
