package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultIdlingVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705657333)
        val items = mutableListOf<DefaultIdlingVariance>()
        for (index in 0 until 3) {
            items.add(newDefaultIdlingVariance("default-idling-variance-$index", "sample"))
        }
        val report = buildDefaultIdlingVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
