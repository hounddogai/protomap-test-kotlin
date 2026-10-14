package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDutyPeriodAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708436130)
        val items = mutableListOf<SeasonalDutyPeriodAssignment>()
        for (index in 0 until 3) {
            items.add(newSeasonalDutyPeriodAssignment("seasonal-duty-period-assignment-$index", "sample"))
        }
        val report = buildSeasonalDutyPeriodAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
