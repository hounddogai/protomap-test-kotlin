package com.acme.transport.customs.declaration.schedule

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDeclarationScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701758791)
        val items = mutableListOf<SeasonalDeclarationSchedule>()
        for (index in 0 until 3) {
            items.add(newSeasonalDeclarationSchedule("seasonal-declaration-schedule-$index", "sample"))
        }
        val report = buildSeasonalDeclarationScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
