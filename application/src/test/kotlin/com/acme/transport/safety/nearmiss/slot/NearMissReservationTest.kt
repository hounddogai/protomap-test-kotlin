package com.acme.transport.safety.nearmiss.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissReservationTest {
    @Test
    fun `starts in draft`() {
        val item = newNearMissReservation("code-110", "label-503")
        assertEquals(NearMissReservationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newNearMissReservation("code-110", "label-503")
        try {
            item.moveTo(NearMissReservationStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissReservationStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newNearMissReservation("code-110", "label-503")
        item.status = NearMissReservationStatus.CLOSED
        assertFails("move") { item.moveTo(NearMissReservationStatus.DRAFT) }
    }
}
