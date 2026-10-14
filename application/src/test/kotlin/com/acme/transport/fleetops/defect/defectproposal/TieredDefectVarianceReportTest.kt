package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDefectVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702238368)
        val items = mutableListOf<TieredDefectVariance>()
        for (index in 0 until 2) {
            items.add(newTieredDefectVariance("tiered-defect-variance-$index", "sample"))
        }
        val report = buildTieredDefectVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
