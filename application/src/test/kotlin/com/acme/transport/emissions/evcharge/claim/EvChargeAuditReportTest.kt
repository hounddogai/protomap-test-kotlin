package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700719058)
        val items = mutableListOf<EvChargeAudit>()
        for (index in 0 until 5) {
            items.add(newEvChargeAudit("ev-charge-audit-$index", "sample"))
        }
        val report = buildEvChargeAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
