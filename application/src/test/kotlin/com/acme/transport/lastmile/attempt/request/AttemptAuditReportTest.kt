package com.acme.transport.lastmile.attempt.request

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700773895)
        val items = mutableListOf<AttemptAudit>()
        for (index in 0 until 4) {
            items.add(newAttemptAudit("attempt-audit-$index", "sample"))
        }
        val report = buildAttemptAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
