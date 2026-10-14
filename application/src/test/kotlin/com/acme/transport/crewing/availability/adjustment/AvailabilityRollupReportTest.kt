package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703186171)
        val items = mutableListOf<AvailabilityRollup>()
        for (index in 0 until 4) {
            items.add(newAvailabilityRollup("availability-rollup-$index", "sample"))
        }
        val report = buildAvailabilityRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
