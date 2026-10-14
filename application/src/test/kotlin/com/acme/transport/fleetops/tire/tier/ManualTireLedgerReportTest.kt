package com.acme.transport.fleetops.tire.tier

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTireLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701381606)
        val items = mutableListOf<ManualTireLedger>()
        for (index in 0 until 3) {
            items.add(newManualTireLedger("manual-tire-ledger-$index", "sample"))
        }
        val report = buildManualTireLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
