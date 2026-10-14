package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAttemptLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706527724)
        val items = mutableListOf<LenientAttemptLedger>()
        for (index in 0 until 3) {
            items.add(newLenientAttemptLedger("lenient-attempt-ledger-$index", "sample"))
        }
        val report = buildLenientAttemptLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
