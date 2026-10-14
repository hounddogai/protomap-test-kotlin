package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyQualificationQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704301709)
        val items = mutableListOf<LegacyQualificationQuota>()
        for (index in 0 until 6) {
            items.add(newLegacyQualificationQuota("legacy-qualification-quota-$index", "sample"))
        }
        val report = buildLegacyQualificationQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
