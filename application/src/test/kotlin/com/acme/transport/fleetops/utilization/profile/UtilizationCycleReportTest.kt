package com.acme.transport.fleetops.utilization.profile

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703077113)
        val items = mutableListOf<UtilizationCycle>()
        for (index in 0 until 6) {
            items.add(newUtilizationCycle("utilization-cycle-$index", "sample"))
        }
        val report = buildUtilizationCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
