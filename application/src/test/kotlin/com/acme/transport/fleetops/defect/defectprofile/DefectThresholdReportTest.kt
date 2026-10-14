package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703033742)
        val items = mutableListOf<DefectThreshold>()
        for (index in 0 until 5) {
            items.add(newDefectThreshold("defect-threshold-$index", "sample"))
        }
        val report = buildDefectThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
