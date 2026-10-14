package com.acme.transport.crewing.roster.fallbackrosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRosterForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newFallbackRosterForecast("code-323", "title-810")
        assertEquals(FallbackRosterForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newFallbackRosterForecast("code-323", "title-810")
        try {
            item.moveTo(FallbackRosterForecastStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRosterForecastStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFallbackRosterForecast("code-323", "title-810")
        item.status = FallbackRosterForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(FallbackRosterForecastStatus.PLANNED) }
    }
}
