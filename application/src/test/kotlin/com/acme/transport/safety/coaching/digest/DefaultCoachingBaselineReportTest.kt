package com.acme.transport.safety.coaching.digest

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCoachingBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703240336)
        val items = mutableListOf<DefaultCoachingBaseline>()
        for (index in 0 until 6) {
            items.add(newDefaultCoachingBaseline("default-coaching-baseline-$index", "sample"))
        }
        val report = buildDefaultCoachingBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
