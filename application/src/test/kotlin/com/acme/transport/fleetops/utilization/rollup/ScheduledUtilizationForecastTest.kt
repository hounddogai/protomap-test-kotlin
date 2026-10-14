package com.acme.transport.fleetops.utilization.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledUtilizationForecastTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledUtilizationForecast("code-135", "name-671")
        assertEquals(ScheduledUtilizationForecastStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newScheduledUtilizationForecast("code-135", "name-671")
        try {
            item.moveTo(ScheduledUtilizationForecastStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledUtilizationForecastStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledUtilizationForecast("code-135", "name-671")
        item.status = ScheduledUtilizationForecastStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledUtilizationForecastStatus.OPEN) }
    }
}
