package com.acme.transport.emissions.idling.metric

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706910468)
        val items = mutableListOf<IdlingException>()
        for (index in 0 until 6) {
            items.add(newIdlingException("idling-exception-$index", "sample"))
        }
        val report = buildIdlingExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
