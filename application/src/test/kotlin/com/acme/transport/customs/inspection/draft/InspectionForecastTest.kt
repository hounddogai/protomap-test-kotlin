package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionForecastTest {
    @Test
    fun `starts in requested`() {
        val item = newInspectionForecast("code-610", "title-714")
        assertEquals(InspectionForecastStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newInspectionForecast("code-610", "title-714")
        try {
            item.moveTo(InspectionForecastStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionForecastStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newInspectionForecast("code-610", "title-714")
        item.status = InspectionForecastStatus.EXPIRED
        assertFails("move") { item.moveTo(InspectionForecastStatus.REQUESTED) }
    }
}
