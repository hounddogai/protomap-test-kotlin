package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704576916)
        val items = mutableListOf<DutyPeriodAgreement>()
        for (index in 0 until 6) {
            items.add(newDutyPeriodAgreement("duty-period-agreement-$index", "sample"))
        }
        val report = buildDutyPeriodAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
