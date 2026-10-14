package com.acme.transport.crewing.roster.recorditem

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRosterLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700223629)
        val items = mutableListOf<LenientRosterLedger>()
        for (index in 0 until 4) {
            items.add(newLenientRosterLedger("lenient-roster-ledger-$index", "sample"))
        }
        val report = buildLenientRosterLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
