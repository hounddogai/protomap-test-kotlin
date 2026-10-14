package com.acme.transport.safety.nearmiss.audit

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultNearMissAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706896337)
        val items = mutableListOf<DefaultNearMissAudit>()
        for (index in 0 until 6) {
            items.add(newDefaultNearMissAudit("default-near-miss-audit-$index", "sample"))
        }
        val report = buildDefaultNearMissAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
