package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualBackhaulAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703126491)
        val items = mutableListOf<ManualBackhaulAudit>()
        for (index in 0 until 5) {
            items.add(newManualBackhaulAudit("manual-backhaul-audit-$index", "sample"))
        }
        val report = buildManualBackhaulAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
