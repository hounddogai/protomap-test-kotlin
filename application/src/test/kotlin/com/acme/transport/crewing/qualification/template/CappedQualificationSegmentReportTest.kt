package com.acme.transport.crewing.qualification.template

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedQualificationSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707624907)
        val items = mutableListOf<CappedQualificationSegment>()
        for (index in 0 until 3) {
            items.add(newCappedQualificationSegment("capped-qualification-segment-$index", "sample"))
        }
        val report = buildCappedQualificationSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
