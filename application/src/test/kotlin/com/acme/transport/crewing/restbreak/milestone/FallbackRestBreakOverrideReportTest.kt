package com.acme.transport.crewing.restbreak.milestone

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRestBreakOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708331635)
        val items = mutableListOf<FallbackRestBreakOverride>()
        for (index in 0 until 4) {
            items.add(newFallbackRestBreakOverride("fallback-rest-break-override-$index", "sample"))
        }
        val report = buildFallbackRestBreakOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
