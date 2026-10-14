package com.acme.transport.emissions.idling.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingForecastTest {
    @Test
    fun `starts in open`() {
        val item = newIdlingForecast("code-593", "name-480")
        assertEquals(IdlingForecastStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newIdlingForecast("code-593", "name-480")
        try {
            item.moveTo(IdlingForecastStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingForecastStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newIdlingForecast("code-593", "name-480")
        item.status = IdlingForecastStatus.ARCHIVED
        assertFails("move") { item.moveTo(IdlingForecastStatus.OPEN) }
    }
}
