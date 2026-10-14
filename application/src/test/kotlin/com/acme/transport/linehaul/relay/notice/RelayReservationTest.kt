package com.acme.transport.linehaul.relay.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayReservationTest {
    @Test
    fun `starts in proposed`() {
        val item = newRelayReservation("code-327", "name-768")
        assertEquals(RelayReservationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRelayReservation("code-327", "name-768")
        try {
            item.moveTo(RelayReservationStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayReservationStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRelayReservation("code-327", "name-768")
        item.status = RelayReservationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RelayReservationStatus.PROPOSED) }
    }
}
