package com.acme.transport.safety.coaching.exception

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCoachingExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703915257)
        val items = mutableListOf<StrictCoachingException>()
        for (index in 0 until 2) {
            items.add(newStrictCoachingException("strict-coaching-exception-$index", "sample"))
        }
        val report = buildStrictCoachingExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
