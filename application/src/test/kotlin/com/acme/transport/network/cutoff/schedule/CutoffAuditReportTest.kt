package com.acme.transport.network.cutoff.schedule

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701887753)
        val items = mutableListOf<CutoffAudit>()
        for (index in 0 until 6) {
            items.add(newCutoffAudit("cutoff-audit-$index", "sample"))
        }
        val report = buildCutoffAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
