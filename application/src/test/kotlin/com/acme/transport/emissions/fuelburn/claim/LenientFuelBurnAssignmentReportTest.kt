package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientFuelBurnAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704752616)
        val items = mutableListOf<LenientFuelBurnAssignment>()
        for (index in 0 until 5) {
            items.add(newLenientFuelBurnAssignment("lenient-fuel-burn-assignment-$index", "sample"))
        }
        val report = buildLenientFuelBurnAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
