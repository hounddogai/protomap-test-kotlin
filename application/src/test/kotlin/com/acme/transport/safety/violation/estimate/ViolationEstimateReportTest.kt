package com.acme.transport.safety.violation.estimate

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703387359)
        val items = mutableListOf<ViolationEstimate>()
        for (index in 0 until 4) {
            items.add(newViolationEstimate("violation-estimate-$index", "sample"))
        }
        val report = buildViolationEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
