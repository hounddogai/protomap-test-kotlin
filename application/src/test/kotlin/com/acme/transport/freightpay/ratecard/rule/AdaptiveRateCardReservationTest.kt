package com.acme.transport.freightpay.ratecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveRateCardReservationTest {
    @Test
    fun `starts in requested`() {
        val item = newAdaptiveRateCardReservation("code-129", "name-869")
        assertEquals(AdaptiveRateCardReservationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAdaptiveRateCardReservation("code-129", "name-869")
        try {
            item.moveTo(AdaptiveRateCardReservationStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveRateCardReservationStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAdaptiveRateCardReservation("code-129", "name-869")
        item.status = AdaptiveRateCardReservationStatus.EXPIRED
        assertFails("move") { item.moveTo(AdaptiveRateCardReservationStatus.REQUESTED) }
    }
}
