package com.acme.transport.customs.broker.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyBrokerReservationTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyBrokerReservation("code-760", "label-701")
        assertEquals(LegacyBrokerReservationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLegacyBrokerReservation("code-760", "label-701")
        try {
            item.moveTo(LegacyBrokerReservationStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyBrokerReservationStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyBrokerReservation("code-760", "label-701")
        item.status = LegacyBrokerReservationStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyBrokerReservationStatus.IDLE) }
    }
}
