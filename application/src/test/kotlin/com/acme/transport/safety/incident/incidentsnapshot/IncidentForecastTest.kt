package com.acme.transport.safety.incident.incidentsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentForecastTest {
    @Test
    fun `starts in proposed`() {
        val item = newIncidentForecast("code-196", "title-535")
        assertEquals(IncidentForecastStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newIncidentForecast("code-196", "title-535")
        try {
            item.moveTo(IncidentForecastStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentForecastStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newIncidentForecast("code-196", "title-535")
        item.status = IncidentForecastStatus.WITHDRAWN
        assertFails("move") { item.moveTo(IncidentForecastStatus.PROPOSED) }
    }
}
