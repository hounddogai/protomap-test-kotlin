package com.acme.transport.lastmile.drop.dropreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDropReservationTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackDropReservation("code-949", "title-702")
        assertEquals(FallbackDropReservationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newFallbackDropReservation("code-949", "title-702")
        try {
            item.moveTo(FallbackDropReservationStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDropReservationStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackDropReservation("code-949", "title-702")
        item.status = FallbackDropReservationStatus.FAILED
        assertFails("move") { item.moveTo(FallbackDropReservationStatus.NEW) }
    }
}
