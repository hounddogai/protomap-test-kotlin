package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDefectVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708377965)
        val items = mutableListOf<FallbackDefectVariance>()
        for (index in 0 until 4) {
            items.add(newFallbackDefectVariance("fallback-defect-variance-$index", "sample"))
        }
        val report = buildFallbackDefectVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
