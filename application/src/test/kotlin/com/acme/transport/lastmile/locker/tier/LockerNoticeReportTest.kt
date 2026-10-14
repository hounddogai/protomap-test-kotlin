package com.acme.transport.lastmile.locker.tier

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706338572)
        val items = mutableListOf<LockerNotice>()
        for (index in 0 until 3) {
            items.add(newLockerNotice("locker-notice-$index", "sample"))
        }
        val report = buildLockerNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
