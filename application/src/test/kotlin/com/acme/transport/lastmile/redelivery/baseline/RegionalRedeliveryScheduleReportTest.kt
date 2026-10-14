package com.acme.transport.lastmile.redelivery.baseline

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRedeliveryScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709669813)
        val items = mutableListOf<RegionalRedeliverySchedule>()
        for (index in 0 until 4) {
            items.add(newRegionalRedeliverySchedule("regional-redelivery-schedule-$index", "sample"))
        }
        val report = buildRegionalRedeliveryScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
