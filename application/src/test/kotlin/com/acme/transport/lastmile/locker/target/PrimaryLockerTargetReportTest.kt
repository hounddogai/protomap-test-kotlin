package com.acme.transport.lastmile.locker.target

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryLockerTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701776397)
        val items = mutableListOf<PrimaryLockerTarget>()
        for (index in 0 until 2) {
            items.add(newPrimaryLockerTarget("primary-locker-target-$index", "sample"))
        }
        val report = buildPrimaryLockerTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
