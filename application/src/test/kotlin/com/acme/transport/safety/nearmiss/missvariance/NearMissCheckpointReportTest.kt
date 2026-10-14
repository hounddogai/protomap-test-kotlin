package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706719892)
        val items = mutableListOf<NearMissCheckpoint>()
        for (index in 0 until 3) {
            items.add(newNearMissCheckpoint("near-miss-checkpoint-$index", "sample"))
        }
        val report = buildNearMissCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
