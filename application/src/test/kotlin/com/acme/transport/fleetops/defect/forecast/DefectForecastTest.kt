package com.acme.transport.fleetops.defect.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectForecastTest {
    @Test
    fun `starts in created`() {
        val item = newDefectForecast("code-573", "title-850")
        assertEquals(DefectForecastStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newDefectForecast("code-573", "title-850")
        try {
            item.moveTo(DefectForecastStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectForecastStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefectForecast("code-573", "title-850")
        item.status = DefectForecastStatus.RETIRED
        assertFails("move") { item.moveTo(DefectForecastStatus.CREATED) }
    }
}
