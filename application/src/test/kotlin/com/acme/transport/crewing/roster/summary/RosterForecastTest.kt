package com.acme.transport.crewing.roster.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newRosterForecast("code-222", "label-569")
        assertEquals(RosterForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRosterForecast("code-222", "label-569")
        try {
            item.moveTo(RosterForecastStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterForecastStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRosterForecast("code-222", "label-569")
        item.status = RosterForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(RosterForecastStatus.PLANNED) }
    }
}
