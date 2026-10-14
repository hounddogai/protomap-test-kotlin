package com.acme.transport.lastmile.redelivery.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryReservationTest {
    @Test
    fun `starts in open`() {
        val item = newRedeliveryReservation("code-366", "name-894")
        assertEquals(RedeliveryReservationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRedeliveryReservation("code-366", "name-894")
        try {
            item.moveTo(RedeliveryReservationStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryReservationStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRedeliveryReservation("code-366", "name-894")
        item.status = RedeliveryReservationStatus.ARCHIVED
        assertFails("move") { item.moveTo(RedeliveryReservationStatus.OPEN) }
    }
}
