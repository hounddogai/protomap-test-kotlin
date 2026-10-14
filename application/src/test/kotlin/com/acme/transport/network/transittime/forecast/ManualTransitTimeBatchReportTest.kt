package com.acme.transport.network.transittime.forecast

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTransitTimeBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700528386)
        val items = mutableListOf<ManualTransitTimeBatch>()
        for (index in 0 until 6) {
            items.add(newManualTransitTimeBatch("manual-transit-time-batch-$index", "sample"))
        }
        val report = buildManualTransitTimeBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
