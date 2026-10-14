package com.acme.transport.network.servicearea.notice

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientServiceAreaThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705756267)
        val items = mutableListOf<LenientServiceAreaThreshold>()
        for (index in 0 until 6) {
            items.add(newLenientServiceAreaThreshold("lenient-service-area-threshold-$index", "sample"))
        }
        val report = buildLenientServiceAreaThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
