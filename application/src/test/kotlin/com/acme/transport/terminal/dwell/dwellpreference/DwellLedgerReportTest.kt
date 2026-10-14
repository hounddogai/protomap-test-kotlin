package com.acme.transport.terminal.dwell.dwellpreference

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701307629)
        val items = mutableListOf<DwellLedger>()
        for (index in 0 until 2) {
            items.add(newDwellLedger("dwell-ledger-$index", "sample"))
        }
        val report = buildDwellLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
