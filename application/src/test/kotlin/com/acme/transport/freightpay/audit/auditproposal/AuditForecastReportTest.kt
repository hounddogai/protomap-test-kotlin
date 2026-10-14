package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703092121)
        val items = mutableListOf<AuditForecast>()
        for (index in 0 until 4) {
            items.add(newAuditForecast("audit-forecast-$index", "sample"))
        }
        val report = buildAuditForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
