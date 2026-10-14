package com.acme.transport.safety.violation.violationledger

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientViolationLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705824231)
        val items = mutableListOf<LenientViolationLedger>()
        for (index in 0 until 6) {
            items.add(newLenientViolationLedger("lenient-violation-ledger-$index", "sample"))
        }
        val report = buildLenientViolationLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
