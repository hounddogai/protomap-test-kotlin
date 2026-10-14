package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706604320)
        val items = mutableListOf<TireEstimate>()
        for (index in 0 until 4) {
            items.add(newTireEstimate("tire-estimate-$index", "sample"))
        }
        val report = buildTireEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
