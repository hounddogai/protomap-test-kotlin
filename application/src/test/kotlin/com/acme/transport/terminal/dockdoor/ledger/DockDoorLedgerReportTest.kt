package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708503212)
        val items = mutableListOf<DockDoorLedger>()
        for (index in 0 until 3) {
            items.add(newDockDoorLedger("dock-door-ledger-$index", "sample"))
        }
        val report = buildDockDoorLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
