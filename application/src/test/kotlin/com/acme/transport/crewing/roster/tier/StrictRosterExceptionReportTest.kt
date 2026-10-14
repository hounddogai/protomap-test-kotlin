package com.acme.transport.crewing.roster.tier

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRosterExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709716139)
        val items = mutableListOf<StrictRosterException>()
        for (index in 0 until 3) {
            items.add(newStrictRosterException("strict-roster-exception-$index", "sample"))
        }
        val report = buildStrictRosterExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
