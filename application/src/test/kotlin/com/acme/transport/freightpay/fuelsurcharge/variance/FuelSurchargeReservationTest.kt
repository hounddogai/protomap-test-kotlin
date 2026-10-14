package com.acme.transport.freightpay.fuelsurcharge.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeReservationTest {
    @Test
    fun `starts in planned`() {
        val item = newFuelSurchargeReservation("code-730", "name-637")
        assertEquals(FuelSurchargeReservationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newFuelSurchargeReservation("code-730", "name-637")
        try {
            item.moveTo(FuelSurchargeReservationStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeReservationStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFuelSurchargeReservation("code-730", "name-637")
        item.status = FuelSurchargeReservationStatus.CANCELLED
        assertFails("move") { item.moveTo(FuelSurchargeReservationStatus.PLANNED) }
    }
}
