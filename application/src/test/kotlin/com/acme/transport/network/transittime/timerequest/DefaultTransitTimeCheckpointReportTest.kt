package com.acme.transport.network.transittime.timerequest

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTransitTimeCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703523214)
        val items = mutableListOf<DefaultTransitTimeCheckpoint>()
        for (index in 0 until 2) {
            items.add(newDefaultTransitTimeCheckpoint("default-transit-time-checkpoint-$index", "sample"))
        }
        val report = buildDefaultTransitTimeCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
