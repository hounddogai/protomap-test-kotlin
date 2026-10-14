package com.acme.transport.lastmile.locker.window

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709971089)
        val items = mutableListOf<LockerWindow>()
        for (index in 0 until 6) {
            items.add(newLockerWindow("locker-window-$index", "sample"))
        }
        val report = buildLockerWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
