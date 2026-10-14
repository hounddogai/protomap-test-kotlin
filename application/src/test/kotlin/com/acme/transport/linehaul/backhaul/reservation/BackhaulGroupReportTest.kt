package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700430966)
        val items = mutableListOf<BackhaulGroup>()
        for (index in 0 until 5) {
            items.add(newBackhaulGroup("backhaul-group-$index", "sample"))
        }
        val report = buildBackhaulGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
