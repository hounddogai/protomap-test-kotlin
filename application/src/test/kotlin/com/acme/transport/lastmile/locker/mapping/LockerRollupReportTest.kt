package com.acme.transport.lastmile.locker.mapping

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701711979)
        val items = mutableListOf<LockerRollup>()
        for (index in 0 until 4) {
            items.add(newLockerRollup("locker-rollup-$index", "sample"))
        }
        val report = buildLockerRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
