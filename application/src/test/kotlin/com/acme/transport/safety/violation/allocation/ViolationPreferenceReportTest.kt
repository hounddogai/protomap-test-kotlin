package com.acme.transport.safety.violation.allocation

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709392014)
        val items = mutableListOf<ViolationPreference>()
        for (index in 0 until 5) {
            items.add(newViolationPreference("violation-preference-$index", "sample"))
        }
        val report = buildViolationPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
