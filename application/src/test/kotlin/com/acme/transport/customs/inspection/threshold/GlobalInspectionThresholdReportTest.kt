package com.acme.transport.customs.inspection.threshold

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalInspectionThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700523891)
        val items = mutableListOf<GlobalInspectionThreshold>()
        for (index in 0 until 2) {
            items.add(newGlobalInspectionThreshold("global-inspection-threshold-$index", "sample"))
        }
        val report = buildGlobalInspectionThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
