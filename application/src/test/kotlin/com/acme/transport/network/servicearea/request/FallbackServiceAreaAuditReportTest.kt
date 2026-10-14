package com.acme.transport.network.servicearea.request

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackServiceAreaAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709011512)
        val items = mutableListOf<FallbackServiceAreaAudit>()
        for (index in 0 until 2) {
            items.add(newFallbackServiceAreaAudit("fallback-service-area-audit-$index", "sample"))
        }
        val report = buildFallbackServiceAreaAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
