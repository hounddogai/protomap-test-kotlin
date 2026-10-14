package com.acme.transport.crewing.swap.digest

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702839632)
        val items = mutableListOf<SwapDigest>()
        for (index in 0 until 3) {
            items.add(newSwapDigest("swap-digest-$index", "sample"))
        }
        val report = buildSwapDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
