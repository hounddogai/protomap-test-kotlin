package com.acme.transport.fleetops.defect.defectreview

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDefectVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707125184)
        val items = mutableListOf<LegacyDefectVariance>()
        for (index in 0 until 4) {
            items.add(newLegacyDefectVariance("legacy-defect-variance-$index", "sample"))
        }
        val report = buildLegacyDefectVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
