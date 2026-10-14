package com.acme.transport.fleetops.leasereturn.returnrequest

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveLeaseReturnWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708102079)
        val items = mutableListOf<AdaptiveLeaseReturnWindow>()
        for (index in 0 until 6) {
            items.add(newAdaptiveLeaseReturnWindow("adaptive-lease-return-window-$index", "sample"))
        }
        val report = buildAdaptiveLeaseReturnWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
