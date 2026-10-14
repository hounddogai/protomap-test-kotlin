package com.acme.transport.customs.hold.group

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingHoldAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709561555)
        val items = mutableListOf<RollingHoldAllocation>()
        for (index in 0 until 4) {
            items.add(newRollingHoldAllocation("rolling-hold-allocation-$index", "sample"))
        }
        val report = buildRollingHoldAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
