package com.acme.transport.freightpay.dispute.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newDisputeForecast("code-949", "label-332")
        assertEquals(DisputeForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newDisputeForecast("code-949", "label-332")
        try {
            item.moveTo(DisputeForecastStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeForecastStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDisputeForecast("code-949", "label-332")
        item.status = DisputeForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(DisputeForecastStatus.PLANNED) }
    }
}
