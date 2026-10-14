package com.acme.transport.lastmile.attempt.segment

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAttemptAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705387784)
        val items = mutableListOf<LenientAttemptAudit>()
        for (index in 0 until 4) {
            items.add(newLenientAttemptAudit("lenient-attempt-audit-$index", "sample"))
        }
        val report = buildLenientAttemptAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
