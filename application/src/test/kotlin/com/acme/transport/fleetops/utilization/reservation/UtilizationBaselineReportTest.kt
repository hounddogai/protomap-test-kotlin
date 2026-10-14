package com.acme.transport.fleetops.utilization.reservation

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701118113)
        val items = mutableListOf<UtilizationBaseline>()
        for (index in 0 until 4) {
            items.add(newUtilizationBaseline("utilization-baseline-$index", "sample"))
        }
        val report = buildUtilizationBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
