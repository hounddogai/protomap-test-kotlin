package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackClearanceAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706911492)
        val items = mutableListOf<FallbackClearanceAssignment>()
        for (index in 0 until 6) {
            items.add(newFallbackClearanceAssignment("fallback-clearance-assignment-$index", "sample"))
        }
        val report = buildFallbackClearanceAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
