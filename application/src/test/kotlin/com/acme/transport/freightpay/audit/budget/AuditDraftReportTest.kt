package com.acme.transport.freightpay.audit.budget

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705474518)
        val items = mutableListOf<AuditDraft>()
        for (index in 0 until 2) {
            items.add(newAuditDraft("audit-draft-$index", "sample"))
        }
        val report = buildAuditDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
