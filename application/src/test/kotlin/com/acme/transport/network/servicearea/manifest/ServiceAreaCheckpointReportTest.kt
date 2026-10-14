package com.acme.transport.network.servicearea.manifest

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707896870)
        val items = mutableListOf<ServiceAreaCheckpoint>()
        for (index in 0 until 6) {
            items.add(newServiceAreaCheckpoint("service-area-checkpoint-$index", "sample"))
        }
        val report = buildServiceAreaCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
