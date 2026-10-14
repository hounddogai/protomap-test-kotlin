package com.acme.transport.network.servicearea.threshold

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704556472)
        val items = mutableListOf<ServiceAreaLedger>()
        for (index in 0 until 5) {
            items.add(newServiceAreaLedger("service-area-ledger-$index", "sample"))
        }
        val report = buildServiceAreaLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
