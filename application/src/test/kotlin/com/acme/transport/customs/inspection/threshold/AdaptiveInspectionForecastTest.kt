package com.acme.transport.customs.inspection.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveInspectionForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveInspectionForecast("code-738", "title-401")
        assertEquals(AdaptiveInspectionForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newAdaptiveInspectionForecast("code-738", "title-401")
        try {
            item.moveTo(AdaptiveInspectionForecastStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveInspectionForecastStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveInspectionForecast("code-738", "title-401")
        item.status = AdaptiveInspectionForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveInspectionForecastStatus.PLANNED) }
    }
}
