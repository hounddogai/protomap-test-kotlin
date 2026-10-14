package com.acme.transport.terminal.traileryard.yardforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalTrailerYardForecastTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalTrailerYardForecast("code-401", "title-475")
        assertEquals(RegionalTrailerYardForecastStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRegionalTrailerYardForecast("code-401", "title-475")
        try {
            item.moveTo(RegionalTrailerYardForecastStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalTrailerYardForecastStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalTrailerYardForecast("code-401", "title-475")
        item.status = RegionalTrailerYardForecastStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalTrailerYardForecastStatus.REQUESTED) }
    }
}
