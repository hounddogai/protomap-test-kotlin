package com.acme.transport.freightpay.ratecard.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardReservationTest {
    @Test
    fun `starts in new`() {
        val item = newRateCardReservation("code-112", "name-255")
        assertEquals(RateCardReservationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newRateCardReservation("code-112", "name-255")
        try {
            item.moveTo(RateCardReservationStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardReservationStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRateCardReservation("code-112", "name-255")
        item.status = RateCardReservationStatus.FAILED
        assertFails("move") { item.moveTo(RateCardReservationStatus.NEW) }
    }
}
