package com.acme.transport.crewing.restbreak.schedule

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRestBreakProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701418703)
        val items = mutableListOf<GlobalRestBreakProfile>()
        for (index in 0 until 2) {
            items.add(newGlobalRestBreakProfile("global-rest-break-profile-$index", "sample"))
        }
        val report = buildGlobalRestBreakProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
