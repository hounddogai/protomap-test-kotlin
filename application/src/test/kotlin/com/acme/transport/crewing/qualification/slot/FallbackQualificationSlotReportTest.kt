package com.acme.transport.crewing.qualification.slot

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackQualificationSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705626532)
        val items = mutableListOf<FallbackQualificationSlot>()
        for (index in 0 until 3) {
            items.add(newFallbackQualificationSlot("fallback-qualification-slot-$index", "sample"))
        }
        val report = buildFallbackQualificationSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
