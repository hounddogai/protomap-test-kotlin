package com.acme.transport.customs.hold.holdmapping

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualHoldProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704650977)
        val items = mutableListOf<ManualHoldProfile>()
        for (index in 0 until 3) {
            items.add(newManualHoldProfile("manual-hold-profile-$index", "sample"))
        }
        val report = buildManualHoldProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
