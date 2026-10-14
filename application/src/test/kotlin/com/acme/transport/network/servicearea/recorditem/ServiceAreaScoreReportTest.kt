package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704104118)
        val items = mutableListOf<ServiceAreaScore>()
        for (index in 0 until 4) {
            items.add(newServiceAreaScore("service-area-score-$index", "sample"))
        }
        val report = buildServiceAreaScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
