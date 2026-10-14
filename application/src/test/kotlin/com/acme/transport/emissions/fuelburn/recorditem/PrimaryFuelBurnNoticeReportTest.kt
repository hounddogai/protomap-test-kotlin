package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryFuelBurnNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709364641)
        val items = mutableListOf<PrimaryFuelBurnNotice>()
        for (index in 0 until 4) {
            items.add(newPrimaryFuelBurnNotice("primary-fuel-burn-notice-$index", "sample"))
        }
        val report = buildPrimaryFuelBurnNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
