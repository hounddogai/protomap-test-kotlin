package com.acme.transport.safety.coaching.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedCoachingReservationTest {
    @Test
    fun `starts in new`() {
        val item = newCappedCoachingReservation("code-230", "title-808")
        assertEquals(CappedCoachingReservationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newCappedCoachingReservation("code-230", "title-808")
        try {
            item.moveTo(CappedCoachingReservationStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedCoachingReservationStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedCoachingReservation("code-230", "title-808")
        item.status = CappedCoachingReservationStatus.FAILED
        assertFails("move") { item.moveTo(CappedCoachingReservationStatus.NEW) }
    }
}
