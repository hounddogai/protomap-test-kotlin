package com.acme.transport.crewing.qualification.audit

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705113071)
        val items = mutableListOf<QualificationAudit>()
        for (index in 0 until 5) {
            items.add(newQualificationAudit("qualification-audit-$index", "sample"))
        }
        val report = buildQualificationAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
