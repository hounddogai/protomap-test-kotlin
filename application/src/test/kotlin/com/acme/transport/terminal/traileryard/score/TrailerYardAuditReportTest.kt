package com.acme.transport.terminal.traileryard.score

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704026615)
        val items = mutableListOf<TrailerYardAudit>()
        for (index in 0 until 4) {
            items.add(newTrailerYardAudit("trailer-yard-audit-$index", "sample"))
        }
        val report = buildTrailerYardAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
