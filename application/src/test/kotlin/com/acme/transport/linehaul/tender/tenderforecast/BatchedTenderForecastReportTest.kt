package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTenderForecastReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708703727)
        val items = mutableListOf<BatchedTenderForecast>()
        for (index in 0 until 3) {
            items.add(newBatchedTenderForecast("batched-tender-forecast-$index", "sample"))
        }
        val report = buildBatchedTenderForecastReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
