package com.acme.transport.crewing.restbreak.legacybreakentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakReservationTest {
    @Test
    fun `starts in open`() {
        val item = newRestBreakReservation("code-851", "title-893")
        assertEquals(RestBreakReservationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRestBreakReservation("code-851", "title-893")
        try {
            item.moveTo(RestBreakReservationStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakReservationStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRestBreakReservation("code-851", "title-893")
        item.status = RestBreakReservationStatus.ARCHIVED
        assertFails("move") { item.moveTo(RestBreakReservationStatus.OPEN) }
    }
}
