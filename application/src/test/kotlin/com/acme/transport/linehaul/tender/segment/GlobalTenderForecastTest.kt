package com.acme.transport.linehaul.tender.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTenderForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalTenderForecast("code-359", "label-173")
        assertEquals(GlobalTenderForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newGlobalTenderForecast("code-359", "label-173")
        try {
            item.moveTo(GlobalTenderForecastStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTenderForecastStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalTenderForecast("code-359", "label-173")
        item.status = GlobalTenderForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalTenderForecastStatus.PLANNED) }
    }
}
