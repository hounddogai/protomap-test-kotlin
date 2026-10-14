package com.acme.transport.fleetops.leasereturn.returnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyLeaseReturnReservationTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyLeaseReturnReservation("code-680", "name-969")
        assertEquals(LegacyLeaseReturnReservationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLegacyLeaseReturnReservation("code-680", "name-969")
        try {
            item.moveTo(LegacyLeaseReturnReservationStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyLeaseReturnReservationStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyLeaseReturnReservation("code-680", "name-969")
        item.status = LegacyLeaseReturnReservationStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyLeaseReturnReservationStatus.OPEN) }
    }
}
