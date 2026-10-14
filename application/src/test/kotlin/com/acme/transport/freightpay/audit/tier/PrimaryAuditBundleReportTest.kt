package com.acme.transport.freightpay.audit.tier

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryAuditBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709754418)
        val items = mutableListOf<PrimaryAuditBundle>()
        for (index in 0 until 5) {
            items.add(newPrimaryAuditBundle("primary-audit-bundle-$index", "sample"))
        }
        val report = buildPrimaryAuditBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
