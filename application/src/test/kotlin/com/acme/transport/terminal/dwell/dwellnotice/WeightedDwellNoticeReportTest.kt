package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDwellNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701239778)
        val items = mutableListOf<WeightedDwellNotice>()
        for (index in 0 until 2) {
            items.add(newWeightedDwellNotice("weighted-dwell-notice-$index", "sample"))
        }
        val report = buildWeightedDwellNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
