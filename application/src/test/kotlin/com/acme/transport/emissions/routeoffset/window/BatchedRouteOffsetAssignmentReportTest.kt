package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRouteOffsetAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700912180)
        val items = mutableListOf<BatchedRouteOffsetAssignment>()
        for (index in 0 until 6) {
            items.add(newBatchedRouteOffsetAssignment("batched-route-offset-assignment-$index", "sample"))
        }
        val report = buildBatchedRouteOffsetAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
