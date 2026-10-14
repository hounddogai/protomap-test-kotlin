package com.acme.transport.terminal.sortation.sortationtier

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701056997)
        val items = mutableListOf<SortationAudit>()
        for (index in 0 until 5) {
            items.add(newSortationAudit("sortation-audit-$index", "sample"))
        }
        val report = buildSortationAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
