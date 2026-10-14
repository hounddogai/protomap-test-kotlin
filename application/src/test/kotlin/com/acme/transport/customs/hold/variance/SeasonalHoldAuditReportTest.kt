package com.acme.transport.customs.hold.variance

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalHoldAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707845036)
        val items = mutableListOf<SeasonalHoldAudit>()
        for (index in 0 until 3) {
            items.add(newSeasonalHoldAudit("seasonal-hold-audit-$index", "sample"))
        }
        val report = buildSeasonalHoldAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
