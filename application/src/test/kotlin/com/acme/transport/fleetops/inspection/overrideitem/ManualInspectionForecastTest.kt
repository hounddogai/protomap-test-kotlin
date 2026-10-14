package com.acme.transport.fleetops.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualInspectionForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newManualInspectionForecast("code-852", "title-94")
        assertEquals(ManualInspectionForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newManualInspectionForecast("code-852", "title-94")
        try {
            item.moveTo(ManualInspectionForecastStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualInspectionForecastStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualInspectionForecast("code-852", "title-94")
        item.status = ManualInspectionForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualInspectionForecastStatus.PLANNED) }
    }
}
