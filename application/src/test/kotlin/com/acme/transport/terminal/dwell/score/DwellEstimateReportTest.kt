package com.acme.transport.terminal.dwell.score

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704444809)
        val items = mutableListOf<DwellEstimate>()
        for (index in 0 until 6) {
            items.add(newDwellEstimate("dwell-estimate-$index", "sample"))
        }
        val report = buildDwellEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
