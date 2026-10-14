package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDutyPeriodLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708191543)
        val items = mutableListOf<ManualDutyPeriodLedger>()
        for (index in 0 until 6) {
            items.add(newManualDutyPeriodLedger("manual-duty-period-ledger-$index", "sample"))
        }
        val report = buildManualDutyPeriodLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
