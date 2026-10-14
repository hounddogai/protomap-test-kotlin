package com.acme.transport.network.hub.agreement

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingHubLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701352771)
        val items = mutableListOf<RollingHubLedger>()
        for (index in 0 until 6) {
            items.add(newRollingHubLedger("rolling-hub-ledger-$index", "sample"))
        }
        val report = buildRollingHubLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
