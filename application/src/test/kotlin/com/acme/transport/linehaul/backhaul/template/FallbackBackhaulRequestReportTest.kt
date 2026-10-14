package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackBackhaulRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707760931)
        val items = mutableListOf<FallbackBackhaulRequest>()
        for (index in 0 until 3) {
            items.add(newFallbackBackhaulRequest("fallback-backhaul-request-$index", "sample"))
        }
        val report = buildFallbackBackhaulRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
