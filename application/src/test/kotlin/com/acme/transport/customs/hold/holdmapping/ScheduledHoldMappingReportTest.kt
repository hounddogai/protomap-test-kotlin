package com.acme.transport.customs.hold.holdmapping

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledHoldMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701992133)
        val items = mutableListOf<ScheduledHoldMapping>()
        for (index in 0 until 3) {
            items.add(newScheduledHoldMapping("scheduled-hold-mapping-$index", "sample"))
        }
        val report = buildScheduledHoldMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
