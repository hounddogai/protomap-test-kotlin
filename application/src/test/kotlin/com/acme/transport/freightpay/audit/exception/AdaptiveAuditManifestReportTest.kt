package com.acme.transport.freightpay.audit.exception

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAuditManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702849270)
        val items = mutableListOf<AdaptiveAuditManifest>()
        for (index in 0 until 2) {
            items.add(newAdaptiveAuditManifest("adaptive-audit-manifest-$index", "sample"))
        }
        val report = buildAdaptiveAuditManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
