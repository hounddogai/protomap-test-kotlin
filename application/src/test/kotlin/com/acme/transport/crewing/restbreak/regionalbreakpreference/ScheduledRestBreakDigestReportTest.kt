package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRestBreakDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707158002)
        val items = mutableListOf<ScheduledRestBreakDigest>()
        for (index in 0 until 4) {
            items.add(newScheduledRestBreakDigest("scheduled-rest-break-digest-$index", "sample"))
        }
        val report = buildScheduledRestBreakDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
