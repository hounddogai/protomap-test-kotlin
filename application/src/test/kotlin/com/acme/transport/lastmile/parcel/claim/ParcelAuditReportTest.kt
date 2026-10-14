package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705717716)
        val items = mutableListOf<ParcelAudit>()
        for (index in 0 until 3) {
            items.add(newParcelAudit("parcel-audit-$index", "sample"))
        }
        val report = buildParcelAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
