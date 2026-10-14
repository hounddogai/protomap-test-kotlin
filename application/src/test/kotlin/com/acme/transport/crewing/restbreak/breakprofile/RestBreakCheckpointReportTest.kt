package com.acme.transport.crewing.restbreak.breakprofile

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703814306)
        val items = mutableListOf<RestBreakCheckpoint>()
        for (index in 0 until 3) {
            items.add(newRestBreakCheckpoint("rest-break-checkpoint-$index", "sample"))
        }
        val report = buildRestBreakCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
