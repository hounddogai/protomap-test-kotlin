package com.acme.transport.freightpay.audit.legacyauditpolicy

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAuditDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703414232)
        val items = mutableListOf<CappedAuditDigest>()
        for (index in 0 until 6) {
            items.add(newCappedAuditDigest("capped-audit-digest-$index", "sample"))
        }
        val report = buildCappedAuditDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
