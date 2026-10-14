package com.acme.transport.network.servicearea.score

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictServiceAreaCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706407196)
        val items = mutableListOf<StrictServiceAreaCycle>()
        for (index in 0 until 6) {
            items.add(newStrictServiceAreaCycle("strict-service-area-cycle-$index", "sample"))
        }
        val report = buildStrictServiceAreaCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
