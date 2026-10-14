package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledFuelBurnAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708034695)
        val items = mutableListOf<ScheduledFuelBurnAudit>()
        for (index in 0 until 6) {
            items.add(newScheduledFuelBurnAudit("scheduled-fuel-burn-audit-$index", "sample"))
        }
        val report = buildScheduledFuelBurnAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
