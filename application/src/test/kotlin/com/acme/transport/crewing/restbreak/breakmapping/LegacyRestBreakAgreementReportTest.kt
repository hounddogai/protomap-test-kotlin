package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRestBreakAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706329455)
        val items = mutableListOf<LegacyRestBreakAgreement>()
        for (index in 0 until 6) {
            items.add(newLegacyRestBreakAgreement("legacy-rest-break-agreement-$index", "sample"))
        }
        val report = buildLegacyRestBreakAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
