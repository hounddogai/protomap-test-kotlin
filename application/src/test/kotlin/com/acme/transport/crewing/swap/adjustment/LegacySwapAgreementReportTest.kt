package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacySwapAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708343359)
        val items = mutableListOf<LegacySwapAgreement>()
        for (index in 0 until 6) {
            items.add(newLegacySwapAgreement("legacy-swap-agreement-$index", "sample"))
        }
        val report = buildLegacySwapAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
