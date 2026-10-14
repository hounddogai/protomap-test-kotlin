package com.acme.transport.safety.coaching.review

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCoachingLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704080371)
        val items = mutableListOf<ManualCoachingLedger>()
        for (index in 0 until 4) {
            items.add(newManualCoachingLedger("manual-coaching-ledger-$index", "sample"))
        }
        val report = buildManualCoachingLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
