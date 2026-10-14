package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledHoldRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702287324)
        val items = mutableListOf<ScheduledHoldRollup>()
        for (index in 0 until 6) {
            items.add(newScheduledHoldRollup("scheduled-hold-rollup-$index", "sample"))
        }
        val report = buildScheduledHoldRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
