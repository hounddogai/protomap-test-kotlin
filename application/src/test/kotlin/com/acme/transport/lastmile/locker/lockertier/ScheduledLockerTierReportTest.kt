package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLockerTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705044066)
        val items = mutableListOf<ScheduledLockerTier>()
        for (index in 0 until 4) {
            items.add(newScheduledLockerTier("scheduled-locker-tier-$index", "sample"))
        }
        val report = buildScheduledLockerTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
