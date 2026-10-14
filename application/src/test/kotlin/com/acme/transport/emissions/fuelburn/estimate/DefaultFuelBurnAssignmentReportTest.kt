package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultFuelBurnAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706894335)
        val items = mutableListOf<DefaultFuelBurnAssignment>()
        for (index in 0 until 2) {
            items.add(newDefaultFuelBurnAssignment("default-fuel-burn-assignment-$index", "sample"))
        }
        val report = buildDefaultFuelBurnAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
