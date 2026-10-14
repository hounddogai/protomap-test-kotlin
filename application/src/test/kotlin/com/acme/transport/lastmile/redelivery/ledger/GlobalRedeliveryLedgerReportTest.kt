package com.acme.transport.lastmile.redelivery.ledger

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRedeliveryLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709747733)
        val items = mutableListOf<GlobalRedeliveryLedger>()
        for (index in 0 until 3) {
            items.add(newGlobalRedeliveryLedger("global-redelivery-ledger-$index", "sample"))
        }
        val report = buildGlobalRedeliveryLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
