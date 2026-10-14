package com.acme.transport.customs.inspection.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyInspectionForecastTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyInspectionForecast("code-977", "label-864")
        assertEquals(LegacyInspectionForecastStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLegacyInspectionForecast("code-977", "label-864")
        try {
            item.moveTo(LegacyInspectionForecastStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyInspectionForecastStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyInspectionForecast("code-977", "label-864")
        item.status = LegacyInspectionForecastStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyInspectionForecastStatus.PROPOSED) }
    }
}
