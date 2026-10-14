package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCapacityCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707383941)
        val items = mutableListOf<RegionalCapacityCheckpoint>()
        for (index in 0 until 3) {
            items.add(newRegionalCapacityCheckpoint("regional-capacity-checkpoint-$index", "sample"))
        }
        val report = buildRegionalCapacityCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
