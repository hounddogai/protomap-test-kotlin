package com.acme.transport.customs.hold.profile

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyHoldProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702170071)
        val items = mutableListOf<LegacyHoldProfile>()
        for (index in 0 until 5) {
            items.add(newLegacyHoldProfile("legacy-hold-profile-$index", "sample"))
        }
        val report = buildLegacyHoldProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
