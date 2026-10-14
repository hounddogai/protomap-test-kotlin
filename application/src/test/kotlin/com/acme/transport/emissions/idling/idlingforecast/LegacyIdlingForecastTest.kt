package com.acme.transport.emissions.idling.idlingforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyIdlingForecastTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyIdlingForecast("code-29", "title-977")
        assertEquals(LegacyIdlingForecastStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newLegacyIdlingForecast("code-29", "title-977")
        try {
            item.moveTo(LegacyIdlingForecastStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyIdlingForecastStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyIdlingForecast("code-29", "title-977")
        item.status = LegacyIdlingForecastStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyIdlingForecastStatus.OPEN) }
    }
}
