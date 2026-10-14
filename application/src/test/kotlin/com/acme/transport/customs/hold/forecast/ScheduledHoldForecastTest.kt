package com.acme.transport.customs.hold.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledHoldForecastTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledHoldForecast("code-264", "label-328")
        assertEquals(ScheduledHoldForecastStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newScheduledHoldForecast("code-264", "label-328")
        try {
            item.moveTo(ScheduledHoldForecastStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledHoldForecastStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledHoldForecast("code-264", "label-328")
        item.status = ScheduledHoldForecastStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledHoldForecastStatus.NEW) }
    }
}
