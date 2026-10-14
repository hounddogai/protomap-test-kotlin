package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalLockerProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707319325)
        val items = mutableListOf<RegionalLockerProfile>()
        for (index in 0 until 6) {
            items.add(newRegionalLockerProfile("regional-locker-profile-$index", "sample"))
        }
        val report = buildRegionalLockerProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
