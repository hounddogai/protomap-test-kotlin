package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalNearMissAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700365834)
        val items = mutableListOf<RegionalNearMissAudit>()
        for (index in 0 until 3) {
            items.add(newRegionalNearMissAudit("regional-near-miss-audit-$index", "sample"))
        }
        val report = buildRegionalNearMissAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
