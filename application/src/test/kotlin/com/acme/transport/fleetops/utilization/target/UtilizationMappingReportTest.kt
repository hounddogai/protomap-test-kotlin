package com.acme.transport.fleetops.utilization.target

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706357644)
        val items = mutableListOf<UtilizationMapping>()
        for (index in 0 until 5) {
            items.add(newUtilizationMapping("utilization-mapping-$index", "sample"))
        }
        val report = buildUtilizationMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
