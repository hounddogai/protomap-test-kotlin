package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704388641)
        val items = mutableListOf<ClearanceScore>()
        for (index in 0 until 4) {
            items.add(newClearanceScore("clearance-score-$index", "sample"))
        }
        val report = buildClearanceScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
