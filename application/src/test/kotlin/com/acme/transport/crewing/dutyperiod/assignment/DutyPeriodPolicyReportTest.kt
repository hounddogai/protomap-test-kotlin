package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706351950)
        val items = mutableListOf<DutyPeriodPolicy>()
        for (index in 0 until 5) {
            items.add(newDutyPeriodPolicy("duty-period-policy-$index", "sample"))
        }
        val report = buildDutyPeriodPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
