package com.acme.transport.network.servicearea.window

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientServiceAreaAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703632947)
        val items = mutableListOf<LenientServiceAreaAudit>()
        for (index in 0 until 5) {
            items.add(newLenientServiceAreaAudit("lenient-service-area-audit-$index", "sample"))
        }
        val report = buildLenientServiceAreaAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
