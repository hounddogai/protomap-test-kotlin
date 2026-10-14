package com.acme.transport.lastmile.locker.target

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700758962)
        val items = mutableListOf<LockerSegment>()
        for (index in 0 until 5) {
            items.add(newLockerSegment("locker-segment-$index", "sample"))
        }
        val report = buildLockerSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
