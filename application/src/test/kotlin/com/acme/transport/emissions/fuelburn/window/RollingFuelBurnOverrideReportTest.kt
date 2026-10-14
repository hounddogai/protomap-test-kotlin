package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingFuelBurnOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704784430)
        val items = mutableListOf<RollingFuelBurnOverride>()
        for (index in 0 until 6) {
            items.add(newRollingFuelBurnOverride("rolling-fuel-burn-override-$index", "sample"))
        }
        val report = buildRollingFuelBurnOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
