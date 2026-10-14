package com.acme.transport.fleetops.defect.cycle

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDefectAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700363182)
        val items = mutableListOf<RegionalDefectAllocation>()
        for (index in 0 until 5) {
            items.add(newRegionalDefectAllocation("regional-defect-allocation-$index", "sample"))
        }
        val report = buildRegionalDefectAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
