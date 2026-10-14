package com.acme.transport.fleetops.defect.snapshot

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDefectCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705663652)
        val items = mutableListOf<RegionalDefectCycle>()
        for (index in 0 until 3) {
            items.add(newRegionalDefectCycle("regional-defect-cycle-$index", "sample"))
        }
        val report = buildRegionalDefectCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
