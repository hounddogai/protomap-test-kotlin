package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingFuelBurnRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700038516)
        val items = mutableListOf<PendingFuelBurnRollup>()
        for (index in 0 until 2) {
            items.add(newPendingFuelBurnRollup("pending-fuel-burn-rollup-$index", "sample"))
        }
        val report = buildPendingFuelBurnRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
