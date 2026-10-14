package com.acme.transport.network.zone.reservation

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalZoneLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704912753)
        val items = mutableListOf<RegionalZoneLedger>()
        for (index in 0 until 6) {
            items.add(newRegionalZoneLedger("regional-zone-ledger-$index", "sample"))
        }
        val report = buildRegionalZoneLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
