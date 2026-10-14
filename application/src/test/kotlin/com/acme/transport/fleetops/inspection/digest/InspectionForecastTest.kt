package com.acme.transport.fleetops.inspection.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionForecastTest {
    @Test
    fun `starts in requested`() {
        val item = newInspectionForecast("code-484", "name-629")
        assertEquals(InspectionForecastStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newInspectionForecast("code-484", "name-629")
        try {
            item.moveTo(InspectionForecastStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionForecastStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newInspectionForecast("code-484", "name-629")
        item.status = InspectionForecastStatus.EXPIRED
        assertFails("move") { item.moveTo(InspectionForecastStatus.REQUESTED) }
    }
}
