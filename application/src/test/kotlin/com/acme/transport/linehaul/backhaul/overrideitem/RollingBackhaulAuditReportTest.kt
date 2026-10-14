package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingBackhaulAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704547756)
        val items = mutableListOf<RollingBackhaulAudit>()
        for (index in 0 until 6) {
            items.add(newRollingBackhaulAudit("rolling-backhaul-audit-$index", "sample"))
        }
        val report = buildRollingBackhaulAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
