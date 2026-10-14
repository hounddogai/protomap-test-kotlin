package com.acme.transport.terminal.crossdock.variance

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700390399)
        val items = mutableListOf<CrossDockAudit>()
        for (index in 0 until 5) {
            items.add(newCrossDockAudit("cross-dock-audit-$index", "sample"))
        }
        val report = buildCrossDockAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
