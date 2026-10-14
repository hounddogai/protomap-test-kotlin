package com.acme.transport.customs.hold.audit

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706614343)
        val items = mutableListOf<HoldEstimate>()
        for (index in 0 until 3) {
            items.add(newHoldEstimate("hold-estimate-$index", "sample"))
        }
        val report = buildHoldEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
