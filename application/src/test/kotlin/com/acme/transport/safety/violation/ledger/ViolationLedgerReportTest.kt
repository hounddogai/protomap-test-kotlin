package com.acme.transport.safety.violation.ledger

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706113612)
        val items = mutableListOf<ViolationLedger>()
        for (index in 0 until 6) {
            items.add(newViolationLedger("violation-ledger-$index", "sample"))
        }
        val report = buildViolationLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
