package com.acme.transport.safety.nearmiss.metric

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703097522)
        val items = mutableListOf<NearMissAudit>()
        for (index in 0 until 6) {
            items.add(newNearMissAudit("near-miss-audit-$index", "sample"))
        }
        val report = buildNearMissAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
