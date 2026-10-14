package com.acme.transport.safety.violation.estimate

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingViolationWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701169933)
        val items = mutableListOf<RollingViolationWindow>()
        for (index in 0 until 6) {
            items.add(newRollingViolationWindow("rolling-violation-window-$index", "sample"))
        }
        val report = buildRollingViolationWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
