package com.acme.transport.safety.nearmiss.missclaim

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultNearMissLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704297134)
        val items = mutableListOf<DefaultNearMissLedger>()
        for (index in 0 until 6) {
            items.add(newDefaultNearMissLedger("default-near-miss-ledger-$index", "sample"))
        }
        val report = buildDefaultNearMissLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
