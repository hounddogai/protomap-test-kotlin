package com.acme.transport.crewing.roster.rosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRosterForecastTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientRosterForecast("code-226", "name-550")
        assertEquals(LenientRosterForecastStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLenientRosterForecast("code-226", "name-550")
        try {
            item.moveTo(LenientRosterForecastStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRosterForecastStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientRosterForecast("code-226", "name-550")
        item.status = LenientRosterForecastStatus.STOPPED
        assertFails("move") { item.moveTo(LenientRosterForecastStatus.IDLE) }
    }
}
