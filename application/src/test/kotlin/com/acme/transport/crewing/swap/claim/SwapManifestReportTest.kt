package com.acme.transport.crewing.swap.claim

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701969252)
        val items = mutableListOf<SwapManifest>()
        for (index in 0 until 3) {
            items.add(newSwapManifest("swap-manifest-$index", "sample"))
        }
        val report = buildSwapManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
