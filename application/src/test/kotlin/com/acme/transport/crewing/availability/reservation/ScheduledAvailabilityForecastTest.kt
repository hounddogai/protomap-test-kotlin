package com.acme.transport.crewing.availability.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledAvailabilityForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledAvailabilityForecast("code-861", "title-47")
        assertEquals(ScheduledAvailabilityForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newScheduledAvailabilityForecast("code-861", "title-47")
        try {
            item.moveTo(ScheduledAvailabilityForecastStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledAvailabilityForecastStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledAvailabilityForecast("code-861", "title-47")
        item.status = ScheduledAvailabilityForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledAvailabilityForecastStatus.PLANNED) }
    }
}
