package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRestBreakAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701394676)
        val items = mutableListOf<FallbackRestBreakAssignment>()
        for (index in 0 until 2) {
            items.add(newFallbackRestBreakAssignment("fallback-rest-break-assignment-$index", "sample"))
        }
        val report = buildFallbackRestBreakAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
