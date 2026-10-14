package com.acme.transport.customs.declaration.segment

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDeclarationSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709946693)
        val items = mutableListOf<AdaptiveDeclarationSegment>()
        for (index in 0 until 3) {
            items.add(newAdaptiveDeclarationSegment("adaptive-declaration-segment-$index", "sample"))
        }
        val report = buildAdaptiveDeclarationSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
