package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700419576)
        val items = mutableListOf<DutyPeriodClaim>()
        for (index in 0 until 3) {
            items.add(newDutyPeriodClaim("duty-period-claim-$index", "sample"))
        }
        val report = buildDutyPeriodClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
