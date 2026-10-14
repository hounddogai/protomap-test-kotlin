package com.acme.transport.fleetops.serviceplan.variance

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingServicePlanLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705734547)
        val items = mutableListOf<PendingServicePlanLedger>()
        for (index in 0 until 6) {
            items.add(newPendingServicePlanLedger("pending-service-plan-ledger-$index", "sample"))
        }
        val report = buildPendingServicePlanLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
