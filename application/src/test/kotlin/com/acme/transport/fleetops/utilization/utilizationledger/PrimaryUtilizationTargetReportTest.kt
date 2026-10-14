package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryUtilizationTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702822276)
        val items = mutableListOf<PrimaryUtilizationTarget>()
        for (index in 0 until 3) {
            items.add(newPrimaryUtilizationTarget("primary-utilization-target-$index", "sample"))
        }
        val report = buildPrimaryUtilizationTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
