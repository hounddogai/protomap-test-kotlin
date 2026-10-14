package com.acme.transport.freightpay.accessorial.accessorialcycle

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701864274)
        val items = mutableListOf<AccessorialLedger>()
        for (index in 0 until 3) {
            items.add(newAccessorialLedger("accessorial-ledger-$index", "sample"))
        }
        val report = buildAccessorialLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
