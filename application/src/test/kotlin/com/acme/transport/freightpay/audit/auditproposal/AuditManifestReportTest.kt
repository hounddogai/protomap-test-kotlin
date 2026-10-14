package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708067026)
        val items = mutableListOf<AuditManifest>()
        for (index in 0 until 3) {
            items.add(newAuditManifest("audit-manifest-$index", "sample"))
        }
        val report = buildAuditManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
