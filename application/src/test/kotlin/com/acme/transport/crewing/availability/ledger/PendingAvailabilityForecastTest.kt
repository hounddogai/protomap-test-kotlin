package com.acme.transport.crewing.availability.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingAvailabilityForecastTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingAvailabilityForecast("code-194", "name-533")
        assertEquals(PendingAvailabilityForecastStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newPendingAvailabilityForecast("code-194", "name-533")
        try {
            item.moveTo(PendingAvailabilityForecastStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingAvailabilityForecastStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingAvailabilityForecast("code-194", "name-533")
        item.status = PendingAvailabilityForecastStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingAvailabilityForecastStatus.PROPOSED) }
    }
}
