package com.acme.transport.linehaul.relay.relayassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualRelayReservationTest {
    @Test
    fun `starts in new`() {
        val item = newManualRelayReservation("code-960", "title-162")
        assertEquals(ManualRelayReservationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newManualRelayReservation("code-960", "title-162")
        try {
            item.moveTo(ManualRelayReservationStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualRelayReservationStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualRelayReservation("code-960", "title-162")
        item.status = ManualRelayReservationStatus.FAILED
        assertFails("move") { item.moveTo(ManualRelayReservationStatus.NEW) }
    }
}
