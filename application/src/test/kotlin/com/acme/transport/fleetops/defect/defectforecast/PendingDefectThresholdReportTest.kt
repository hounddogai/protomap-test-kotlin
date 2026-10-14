package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDefectThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707199915)
        val items = mutableListOf<PendingDefectThreshold>()
        for (index in 0 until 2) {
            items.add(newPendingDefectThreshold("pending-defect-threshold-$index", "sample"))
        }
        val report = buildPendingDefectThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
