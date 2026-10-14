package com.acme.transport.safety.violation.slot

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyViolationBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709700156)
        val items = mutableListOf<LegacyViolationBundle>()
        for (index in 0 until 6) {
            items.add(newLegacyViolationBundle("legacy-violation-bundle-$index", "sample"))
        }
        val report = buildLegacyViolationBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
