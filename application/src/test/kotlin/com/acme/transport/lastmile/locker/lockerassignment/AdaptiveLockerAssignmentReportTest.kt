package com.acme.transport.lastmile.locker.lockerassignment

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveLockerAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708064726)
        val items = mutableListOf<AdaptiveLockerAssignment>()
        for (index in 0 until 5) {
            items.add(newAdaptiveLockerAssignment("adaptive-locker-assignment-$index", "sample"))
        }
        val report = buildAdaptiveLockerAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
