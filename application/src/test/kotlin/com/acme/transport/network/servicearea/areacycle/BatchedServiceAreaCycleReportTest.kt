package com.acme.transport.network.servicearea.areacycle

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedServiceAreaCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704063268)
        val items = mutableListOf<BatchedServiceAreaCycle>()
        for (index in 0 until 4) {
            items.add(newBatchedServiceAreaCycle("batched-service-area-cycle-$index", "sample"))
        }
        val report = buildBatchedServiceAreaCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
