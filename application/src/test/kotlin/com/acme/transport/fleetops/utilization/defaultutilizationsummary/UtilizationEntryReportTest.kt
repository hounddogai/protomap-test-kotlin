package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700359807)
        val items = mutableListOf<UtilizationEntry>()
        for (index in 0 until 2) {
            items.add(newUtilizationEntry("utilization-entry-$index", "sample"))
        }
        val report = buildUtilizationEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
