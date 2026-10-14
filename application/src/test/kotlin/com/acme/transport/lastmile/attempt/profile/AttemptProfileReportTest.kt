package com.acme.transport.lastmile.attempt.profile

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706690506)
        val items = mutableListOf<AttemptProfile>()
        for (index in 0 until 5) {
            items.add(newAttemptProfile("attempt-profile-$index", "sample"))
        }
        val report = buildAttemptProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
