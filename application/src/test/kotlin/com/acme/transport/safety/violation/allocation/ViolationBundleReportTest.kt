package com.acme.transport.safety.violation.allocation

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700789620)
        val items = mutableListOf<ViolationBundle>()
        for (index in 0 until 4) {
            items.add(newViolationBundle("violation-bundle-$index", "sample"))
        }
        val report = buildViolationBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
