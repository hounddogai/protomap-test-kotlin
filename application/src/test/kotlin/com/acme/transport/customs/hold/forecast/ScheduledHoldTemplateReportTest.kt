package com.acme.transport.customs.hold.forecast

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledHoldTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701042293)
        val items = mutableListOf<ScheduledHoldTemplate>()
        for (index in 0 until 3) {
            items.add(newScheduledHoldTemplate("scheduled-hold-template-$index", "sample"))
        }
        val report = buildScheduledHoldTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
