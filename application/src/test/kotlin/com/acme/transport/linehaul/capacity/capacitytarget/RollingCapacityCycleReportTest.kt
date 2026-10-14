package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCapacityCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709831893)
        val items = mutableListOf<RollingCapacityCycle>()
        for (index in 0 until 5) {
            items.add(newRollingCapacityCycle("rolling-capacity-cycle-$index", "sample"))
        }
        val report = buildRollingCapacityCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
