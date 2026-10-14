package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalIdlingExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702027056)
        val items = mutableListOf<RegionalIdlingException>()
        for (index in 0 until 5) {
            items.add(newRegionalIdlingException("regional-idling-exception-$index", "sample"))
        }
        val report = buildRegionalIdlingExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
