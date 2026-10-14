package com.acme.transport.linehaul.tender.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTenderReservationTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictTenderReservation("code-402", "title-277")
        assertEquals(StrictTenderReservationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newStrictTenderReservation("code-402", "title-277")
        try {
            item.moveTo(StrictTenderReservationStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTenderReservationStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictTenderReservation("code-402", "title-277")
        item.status = StrictTenderReservationStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictTenderReservationStatus.PLANNED) }
    }
}
