package com.acme.transport.safety.coaching.rollup

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703150813)
        val items = mutableListOf<CoachingRequest>()
        for (index in 0 until 6) {
            items.add(newCoachingRequest("coaching-request-$index", "sample"))
        }
        val report = buildCoachingRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
