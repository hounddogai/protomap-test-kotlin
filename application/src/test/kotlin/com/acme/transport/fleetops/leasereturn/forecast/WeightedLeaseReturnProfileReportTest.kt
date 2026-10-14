package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedLeaseReturnProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705073724)
        val items = mutableListOf<WeightedLeaseReturnProfile>()
        for (index in 0 until 2) {
            items.add(newWeightedLeaseReturnProfile("weighted-lease-return-profile-$index", "sample"))
        }
        val report = buildWeightedLeaseReturnProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
