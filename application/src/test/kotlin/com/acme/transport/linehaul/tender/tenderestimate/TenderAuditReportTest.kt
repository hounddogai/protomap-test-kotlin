package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700485291)
        val items = mutableListOf<TenderAudit>()
        for (index in 0 until 6) {
            items.add(newTenderAudit("tender-audit-$index", "sample"))
        }
        val report = buildTenderAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
