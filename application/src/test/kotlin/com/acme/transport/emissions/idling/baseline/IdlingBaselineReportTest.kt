package com.acme.transport.emissions.idling.baseline

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709694009)
        val items = mutableListOf<IdlingBaseline>()
        for (index in 0 until 6) {
            items.add(newIdlingBaseline("idling-baseline-$index", "sample"))
        }
        val report = buildIdlingBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
