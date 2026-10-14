package com.acme.transport.fleetops.serviceplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledServicePlanForecastTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledServicePlanForecast("code-484", "label-30")
        assertEquals(ScheduledServicePlanForecastStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newScheduledServicePlanForecast("code-484", "label-30")
        try {
            item.moveTo(ScheduledServicePlanForecastStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledServicePlanForecastStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledServicePlanForecast("code-484", "label-30")
        item.status = ScheduledServicePlanForecastStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledServicePlanForecastStatus.IDLE) }
    }
}
