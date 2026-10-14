package com.acme.transport.customs.broker.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerReservationTest {
    @Test
    fun `starts in requested`() {
        val item = newBrokerReservation("code-462", "title-941")
        assertEquals(BrokerReservationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newBrokerReservation("code-462", "title-941")
        try {
            item.moveTo(BrokerReservationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerReservationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBrokerReservation("code-462", "title-941")
        item.status = BrokerReservationStatus.EXPIRED
        assertFails("move") { item.moveTo(BrokerReservationStatus.REQUESTED) }
    }
}
