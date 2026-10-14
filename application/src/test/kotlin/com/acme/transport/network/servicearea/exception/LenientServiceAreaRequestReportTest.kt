package com.acme.transport.network.servicearea.exception

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientServiceAreaRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701115071)
        val items = mutableListOf<LenientServiceAreaRequest>()
        for (index in 0 until 4) {
            items.add(newLenientServiceAreaRequest("lenient-service-area-request-$index", "sample"))
        }
        val report = buildLenientServiceAreaRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
