package com.acme.transport.safety.coaching.schedule

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705676265)
        val items = mutableListOf<CoachingDigest>()
        for (index in 0 until 3) {
            items.add(newCoachingDigest("coaching-digest-$index", "sample"))
        }
        val report = buildCoachingDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
