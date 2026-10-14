package com.acme.transport.lastmile.attempt.exception

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultAttemptNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708717960)
        val items = mutableListOf<DefaultAttemptNotice>()
        for (index in 0 until 2) {
            items.add(newDefaultAttemptNotice("default-attempt-notice-$index", "sample"))
        }
        val report = buildDefaultAttemptNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
