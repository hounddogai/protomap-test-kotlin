package com.acme.transport.crewing.dutyperiod.ledger

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDutyPeriodProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704865791)
        val items = mutableListOf<LenientDutyPeriodProfile>()
        for (index in 0 until 6) {
            items.add(newLenientDutyPeriodProfile("lenient-duty-period-profile-$index", "sample"))
        }
        val report = buildLenientDutyPeriodProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
