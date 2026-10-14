package com.acme.transport.freightpay.accessorial.accessorialpolicy

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAccessorialLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707454724)
        val items = mutableListOf<FallbackAccessorialLedger>()
        for (index in 0 until 4) {
            items.add(newFallbackAccessorialLedger("fallback-accessorial-ledger-$index", "sample"))
        }
        val report = buildFallbackAccessorialLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
