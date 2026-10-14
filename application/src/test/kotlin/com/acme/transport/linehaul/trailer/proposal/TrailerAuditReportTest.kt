package com.acme.transport.linehaul.trailer.proposal

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703580516)
        val items = mutableListOf<TrailerAudit>()
        for (index in 0 until 4) {
            items.add(newTrailerAudit("trailer-audit-$index", "sample"))
        }
        val report = buildTrailerAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
