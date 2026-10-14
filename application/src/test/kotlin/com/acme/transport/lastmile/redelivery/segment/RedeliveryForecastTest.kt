package com.acme.transport.lastmile.redelivery.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newRedeliveryForecast("code-679", "title-633")
        assertEquals(RedeliveryForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newRedeliveryForecast("code-679", "title-633")
        try {
            item.moveTo(RedeliveryForecastStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryForecastStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRedeliveryForecast("code-679", "title-633")
        item.status = RedeliveryForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(RedeliveryForecastStatus.PLANNED) }
    }
}
