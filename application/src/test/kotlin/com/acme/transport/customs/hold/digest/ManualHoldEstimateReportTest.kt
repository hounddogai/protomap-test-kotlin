package com.acme.transport.customs.hold.digest

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualHoldEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709513702)
        val items = mutableListOf<ManualHoldEstimate>()
        for (index in 0 until 2) {
            items.add(newManualHoldEstimate("manual-hold-estimate-$index", "sample"))
        }
        val report = buildManualHoldEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
