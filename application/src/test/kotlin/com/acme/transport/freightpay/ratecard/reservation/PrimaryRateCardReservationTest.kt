package com.acme.transport.freightpay.ratecard.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRateCardReservationTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryRateCardReservation("code-681", "label-231")
        assertEquals(PrimaryRateCardReservationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newPrimaryRateCardReservation("code-681", "label-231")
        try {
            item.moveTo(PrimaryRateCardReservationStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRateCardReservationStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryRateCardReservation("code-681", "label-231")
        item.status = PrimaryRateCardReservationStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryRateCardReservationStatus.OPEN) }
    }
}
