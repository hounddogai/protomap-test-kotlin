package com.acme.transport.crewing.availability.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newAvailabilityScore("code-695", "title-883")
        assertEquals(AvailabilityScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newAvailabilityScore("code-695", "title-883")
        try {
            item.moveTo(AvailabilityScoreStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityScoreStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAvailabilityScore("code-695", "title-883")
        item.status = AvailabilityScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(AvailabilityScoreStatus.PLANNED) }
    }
}
