package com.acme.transport.linehaul.laneplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newLanePlanForecast("code-616", "title-51")
        assertEquals(LanePlanForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLanePlanForecast("code-616", "title-51")
        try {
            item.moveTo(LanePlanForecastStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanForecastStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLanePlanForecast("code-616", "title-51")
        item.status = LanePlanForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(LanePlanForecastStatus.PLANNED) }
    }
}
