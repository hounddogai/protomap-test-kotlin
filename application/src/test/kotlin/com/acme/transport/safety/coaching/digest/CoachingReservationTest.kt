package com.acme.transport.safety.coaching.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingReservationTest {
    @Test
    fun `starts in open`() {
        val item = newCoachingReservation("code-874", "name-797")
        assertEquals(CoachingReservationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCoachingReservation("code-874", "name-797")
        try {
            item.moveTo(CoachingReservationStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingReservationStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCoachingReservation("code-874", "name-797")
        item.status = CoachingReservationStatus.ARCHIVED
        assertFails("move") { item.moveTo(CoachingReservationStatus.OPEN) }
    }
}
