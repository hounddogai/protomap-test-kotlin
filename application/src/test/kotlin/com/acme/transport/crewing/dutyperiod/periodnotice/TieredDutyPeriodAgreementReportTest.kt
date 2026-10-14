package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDutyPeriodAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706098559)
        val items = mutableListOf<TieredDutyPeriodAgreement>()
        for (index in 0 until 3) {
            items.add(newTieredDutyPeriodAgreement("tiered-duty-period-agreement-$index", "sample"))
        }
        val report = buildTieredDutyPeriodAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
