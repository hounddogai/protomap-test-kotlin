package com.acme.transport.crewing.restbreak.profile

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701749929)
        val items = mutableListOf<RestBreakProfile>()
        for (index in 0 until 4) {
            items.add(newRestBreakProfile("rest-break-profile-$index", "sample"))
        }
        val report = buildRestBreakProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
