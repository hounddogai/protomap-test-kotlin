package com.acme.transport.network.cutoff.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCutoffReservationTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalCutoffReservation("code-971", "name-113")
        assertEquals(GlobalCutoffReservationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newGlobalCutoffReservation("code-971", "name-113")
        try {
            item.moveTo(GlobalCutoffReservationStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCutoffReservationStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalCutoffReservation("code-971", "name-113")
        item.status = GlobalCutoffReservationStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalCutoffReservationStatus.IDLE) }
    }
}
