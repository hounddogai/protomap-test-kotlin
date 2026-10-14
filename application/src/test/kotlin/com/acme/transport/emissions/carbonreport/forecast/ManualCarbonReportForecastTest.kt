package com.acme.transport.emissions.carbonreport.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCarbonReportForecastTest {
    @Test
    fun `starts in draft`() {
        val item = newManualCarbonReportForecast("code-349", "label-70")
        assertEquals(ManualCarbonReportForecastStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newManualCarbonReportForecast("code-349", "label-70")
        try {
            item.moveTo(ManualCarbonReportForecastStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCarbonReportForecastStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualCarbonReportForecast("code-349", "label-70")
        item.status = ManualCarbonReportForecastStatus.CLOSED
        assertFails("move") { item.moveTo(ManualCarbonReportForecastStatus.DRAFT) }
    }
}
