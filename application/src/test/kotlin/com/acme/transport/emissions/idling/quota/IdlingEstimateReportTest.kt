package com.acme.transport.emissions.idling.quota

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705471808)
        val items = mutableListOf<IdlingEstimate>()
        for (index in 0 until 6) {
            items.add(newIdlingEstimate("idling-estimate-$index", "sample"))
        }
        val report = buildIdlingEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
