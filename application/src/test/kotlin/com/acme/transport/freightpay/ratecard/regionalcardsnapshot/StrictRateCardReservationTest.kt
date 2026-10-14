package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRateCardReservationTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictRateCardReservation("code-955", "label-455")
        assertEquals(StrictRateCardReservationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newStrictRateCardReservation("code-955", "label-455")
        try {
            item.moveTo(StrictRateCardReservationStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRateCardReservationStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictRateCardReservation("code-955", "label-455")
        item.status = StrictRateCardReservationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictRateCardReservationStatus.PROPOSED) }
    }
}
