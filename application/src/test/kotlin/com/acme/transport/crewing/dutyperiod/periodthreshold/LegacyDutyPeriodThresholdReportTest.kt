package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDutyPeriodThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701639716)
        val items = mutableListOf<LegacyDutyPeriodThreshold>()
        for (index in 0 until 4) {
            items.add(newLegacyDutyPeriodThreshold("legacy-duty-period-threshold-$index", "sample"))
        }
        val report = buildLegacyDutyPeriodThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
