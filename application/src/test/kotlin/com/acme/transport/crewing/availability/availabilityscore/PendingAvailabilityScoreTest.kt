package com.acme.transport.crewing.availability.availabilityscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingAvailabilityScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingAvailabilityScore("code-496", "title-717")
        assertEquals(PendingAvailabilityScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPendingAvailabilityScore("code-496", "title-717")
        try {
            item.moveTo(PendingAvailabilityScoreStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingAvailabilityScoreStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingAvailabilityScore("code-496", "title-717")
        item.status = PendingAvailabilityScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingAvailabilityScoreStatus.PLANNED) }
    }
}
