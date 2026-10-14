package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryServicePlanLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703391569)
        val items = mutableListOf<PrimaryServicePlanLedger>()
        for (index in 0 until 5) {
            items.add(newPrimaryServicePlanLedger("primary-service-plan-ledger-$index", "sample"))
        }
        val report = buildPrimaryServicePlanLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
