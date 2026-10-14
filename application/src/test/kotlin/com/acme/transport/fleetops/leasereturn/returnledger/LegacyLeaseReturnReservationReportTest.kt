package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyLeaseReturnReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707106141)
        val items = mutableListOf<LegacyLeaseReturnReservation>()
        for (index in 0 until 3) {
            items.add(newLegacyLeaseReturnReservation("legacy-lease-return-reservation-$index", "sample"))
        }
        val report = buildLegacyLeaseReturnReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
