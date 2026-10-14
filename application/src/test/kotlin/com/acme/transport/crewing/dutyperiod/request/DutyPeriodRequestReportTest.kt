package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707746907)
        val items = mutableListOf<DutyPeriodRequest>()
        for (index in 0 until 3) {
            items.add(newDutyPeriodRequest("duty-period-request-$index", "sample"))
        }
        val report = buildDutyPeriodRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
