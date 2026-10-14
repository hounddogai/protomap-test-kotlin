package com.acme.transport.network.cutoff.template

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCutoffEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704713032)
        val items = mutableListOf<DefaultCutoffEstimate>()
        for (index in 0 until 2) {
            items.add(newDefaultCutoffEstimate("default-cutoff-estimate-$index", "sample"))
        }
        val report = buildDefaultCutoffEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
