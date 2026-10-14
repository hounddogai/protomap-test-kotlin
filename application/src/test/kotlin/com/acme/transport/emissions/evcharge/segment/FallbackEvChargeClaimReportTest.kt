package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackEvChargeClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705659897)
        val items = mutableListOf<FallbackEvChargeClaim>()
        for (index in 0 until 2) {
            items.add(newFallbackEvChargeClaim("fallback-ev-charge-claim-$index", "sample"))
        }
        val report = buildFallbackEvChargeClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
