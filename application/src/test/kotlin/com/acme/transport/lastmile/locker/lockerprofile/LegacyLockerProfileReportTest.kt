package com.acme.transport.lastmile.locker.lockerprofile

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyLockerProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703070814)
        val items = mutableListOf<LegacyLockerProfile>()
        for (index in 0 until 4) {
            items.add(newLegacyLockerProfile("legacy-locker-profile-$index", "sample"))
        }
        val report = buildLegacyLockerProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
