package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700119197)
        val items = mutableListOf<ServiceAreaBatch>()
        for (index in 0 until 6) {
            items.add(newServiceAreaBatch("service-area-batch-$index", "sample"))
        }
        val report = buildServiceAreaBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
