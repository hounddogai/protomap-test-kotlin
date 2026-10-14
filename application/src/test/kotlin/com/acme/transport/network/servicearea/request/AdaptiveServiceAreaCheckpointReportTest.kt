package com.acme.transport.network.servicearea.request

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveServiceAreaCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701561969)
        val items = mutableListOf<AdaptiveServiceAreaCheckpoint>()
        for (index in 0 until 3) {
            items.add(newAdaptiveServiceAreaCheckpoint("adaptive-service-area-checkpoint-$index", "sample"))
        }
        val report = buildAdaptiveServiceAreaCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
