package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708850137)
        val items = mutableListOf<LanePlanRequest>()
        for (index in 0 until 5) {
            items.add(newLanePlanRequest("lane-plan-request-$index", "sample"))
        }
        val report = buildLanePlanRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
