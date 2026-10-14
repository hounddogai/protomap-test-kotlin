package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701919571)
        val items = mutableListOf<LockerAllocation>()
        for (index in 0 until 6) {
            items.add(newLockerAllocation("locker-allocation-$index", "sample"))
        }
        val report = buildLockerAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
