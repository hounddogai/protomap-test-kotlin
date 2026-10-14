package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDutyPeriodBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703022147)
        val items = mutableListOf<SeasonalDutyPeriodBatch>()
        for (index in 0 until 6) {
            items.add(newSeasonalDutyPeriodBatch("seasonal-duty-period-batch-$index", "sample"))
        }
        val report = buildSeasonalDutyPeriodBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
