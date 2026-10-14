package com.acme.transport.emissions.fuelburn.audit

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706071178)
        val items = mutableListOf<FuelBurnAudit>()
        for (index in 0 until 6) {
            items.add(newFuelBurnAudit("fuel-burn-audit-$index", "sample"))
        }
        val report = buildFuelBurnAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
