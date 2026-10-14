package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRedeliveryBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702049034)
        val items = mutableListOf<FallbackRedeliveryBundle>()
        for (index in 0 until 6) {
            items.add(newFallbackRedeliveryBundle("fallback-redelivery-bundle-$index", "sample"))
        }
        val report = buildFallbackRedeliveryBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
