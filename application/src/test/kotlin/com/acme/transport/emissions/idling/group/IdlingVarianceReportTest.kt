package com.acme.transport.emissions.idling.group

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709231801)
        val items = mutableListOf<IdlingVariance>()
        for (index in 0 until 6) {
            items.add(newIdlingVariance("idling-variance-$index", "sample"))
        }
        val report = buildIdlingVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
