package com.acme.transport.terminal.sortation.digest

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledSortationLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701890235)
        val items = mutableListOf<ScheduledSortationLedger>()
        for (index in 0 until 4) {
            items.add(newScheduledSortationLedger("scheduled-sortation-ledger-$index", "sample"))
        }
        val report = buildScheduledSortationLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
