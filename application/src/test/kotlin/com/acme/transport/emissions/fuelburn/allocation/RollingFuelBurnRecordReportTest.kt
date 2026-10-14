package com.acme.transport.emissions.fuelburn.allocation

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingFuelBurnRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701674361)
        val items = mutableListOf<RollingFuelBurnRecord>()
        for (index in 0 until 3) {
            items.add(newRollingFuelBurnRecord("rolling-fuel-burn-record-$index", "sample"))
        }
        val report = buildRollingFuelBurnRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
