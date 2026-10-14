package com.acme.transport.lastmile.parcel.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelReservationTest {
    @Test
    fun `starts in open`() {
        val item = newParcelReservation("code-713", "title-147")
        assertEquals(ParcelReservationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newParcelReservation("code-713", "title-147")
        try {
            item.moveTo(ParcelReservationStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelReservationStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newParcelReservation("code-713", "title-147")
        item.status = ParcelReservationStatus.ARCHIVED
        assertFails("move") { item.moveTo(ParcelReservationStatus.OPEN) }
    }
}
