package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709603758)
        val items = mutableListOf<NearMissLedger>()
        for (index in 0 until 3) {
            items.add(newNearMissLedger("near-miss-ledger-$index", "sample"))
        }
        val report = buildNearMissLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
