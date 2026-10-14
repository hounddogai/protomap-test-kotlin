package com.acme.transport.customs.hold.estimate

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredHoldEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704434868)
        val items = mutableListOf<TieredHoldEstimate>()
        for (index in 0 until 2) {
            items.add(newTieredHoldEstimate("tiered-hold-estimate-$index", "sample"))
        }
        val report = buildTieredHoldEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
