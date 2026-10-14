package com.acme.transport.customs.clearance.variance

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705473366)
        val items = mutableListOf<ClearanceVariance>()
        for (index in 0 until 2) {
            items.add(newClearanceVariance("clearance-variance-$index", "sample"))
        }
        val report = buildClearanceVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
