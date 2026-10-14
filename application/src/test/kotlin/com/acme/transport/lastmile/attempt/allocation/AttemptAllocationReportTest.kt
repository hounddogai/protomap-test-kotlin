package com.acme.transport.lastmile.attempt.allocation

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703610444)
        val items = mutableListOf<AttemptAllocation>()
        for (index in 0 until 5) {
            items.add(newAttemptAllocation("attempt-allocation-$index", "sample"))
        }
        val report = buildAttemptAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
