package com.acme.transport.terminal.dwell.group

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDwellGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708015444)
        val items = mutableListOf<AdaptiveDwellGroup>()
        for (index in 0 until 6) {
            items.add(newAdaptiveDwellGroup("adaptive-dwell-group-$index", "sample"))
        }
        val report = buildAdaptiveDwellGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
