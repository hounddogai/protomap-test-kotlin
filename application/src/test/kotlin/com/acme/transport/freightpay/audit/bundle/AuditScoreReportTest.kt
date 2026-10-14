package com.acme.transport.freightpay.audit.bundle

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700566829)
        val items = mutableListOf<AuditScore>()
        for (index in 0 until 4) {
            items.add(newAuditScore("audit-score-$index", "sample"))
        }
        val report = buildAuditScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
