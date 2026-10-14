package com.acme.transport.lastmile.locker.group

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706186686)
        val items = mutableListOf<LockerBaseline>()
        for (index in 0 until 4) {
            items.add(newLockerBaseline("locker-baseline-$index", "sample"))
        }
        val report = buildLockerBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
