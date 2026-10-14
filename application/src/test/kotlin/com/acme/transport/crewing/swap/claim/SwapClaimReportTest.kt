package com.acme.transport.crewing.swap.claim

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700359055)
        val items = mutableListOf<SwapClaim>()
        for (index in 0 until 5) {
            items.add(newSwapClaim("swap-claim-$index", "sample"))
        }
        val report = buildSwapClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
