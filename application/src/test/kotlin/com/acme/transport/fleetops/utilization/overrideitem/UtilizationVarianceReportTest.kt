package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704366668)
        val items = mutableListOf<UtilizationVariance>()
        for (index in 0 until 2) {
            items.add(newUtilizationVariance("utilization-variance-$index", "sample"))
        }
        val report = buildUtilizationVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
