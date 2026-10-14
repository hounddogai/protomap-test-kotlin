package com.acme.transport.crewing.swap.estimate

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709625323)
        val items = mutableListOf<SwapAssignment>()
        for (index in 0 until 5) {
            items.add(newSwapAssignment("swap-assignment-$index", "sample"))
        }
        val report = buildSwapAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
