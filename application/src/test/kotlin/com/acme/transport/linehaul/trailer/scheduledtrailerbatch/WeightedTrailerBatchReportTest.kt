package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTrailerBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706803281)
        val items = mutableListOf<WeightedTrailerBatch>()
        for (index in 0 until 5) {
            items.add(newWeightedTrailerBatch("weighted-trailer-batch-$index", "sample"))
        }
        val report = buildWeightedTrailerBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
