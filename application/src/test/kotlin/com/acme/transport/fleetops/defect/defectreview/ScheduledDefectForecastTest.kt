package com.acme.transport.fleetops.defect.defectreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDefectForecastTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledDefectForecast("code-822", "label-977")
        assertEquals(ScheduledDefectForecastStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newScheduledDefectForecast("code-822", "label-977")
        try {
            item.moveTo(ScheduledDefectForecastStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDefectForecastStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledDefectForecast("code-822", "label-977")
        item.status = ScheduledDefectForecastStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledDefectForecastStatus.DRAFT) }
    }
}
