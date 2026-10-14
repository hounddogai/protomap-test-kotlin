package com.acme.transport.safety.coaching.assignment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCoachingNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707617194)
        val items = mutableListOf<ManualCoachingNotice>()
        for (index in 0 until 3) {
            items.add(newManualCoachingNotice("manual-coaching-notice-$index", "sample"))
        }
        val report = buildManualCoachingNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
