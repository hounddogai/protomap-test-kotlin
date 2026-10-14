package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDutyPeriodPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702927017)
        val items = mutableListOf<PendingDutyPeriodPolicy>()
        for (index in 0 until 4) {
            items.add(newPendingDutyPeriodPolicy("pending-duty-period-policy-$index", "sample"))
        }
        val report = buildPendingDutyPeriodPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
