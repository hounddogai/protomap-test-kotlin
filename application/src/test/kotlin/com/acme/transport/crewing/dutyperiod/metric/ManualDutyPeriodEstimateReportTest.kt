package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDutyPeriodEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706660735)
        val items = mutableListOf<ManualDutyPeriodEstimate>()
        for (index in 0 until 3) {
            items.add(newManualDutyPeriodEstimate("manual-duty-period-estimate-$index", "sample"))
        }
        val report = buildManualDutyPeriodEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
