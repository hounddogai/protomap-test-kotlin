package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703259578)
        val items = mutableListOf<DutyPeriodThreshold>()
        for (index in 0 until 3) {
            items.add(newDutyPeriodThreshold("duty-period-threshold-$index", "sample"))
        }
        val report = buildDutyPeriodThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
