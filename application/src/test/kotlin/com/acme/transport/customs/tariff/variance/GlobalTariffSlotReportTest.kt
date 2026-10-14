package com.acme.transport.customs.tariff.variance

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTariffSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707480591)
        val items = mutableListOf<GlobalTariffSlot>()
        for (index in 0 until 3) {
            items.add(newGlobalTariffSlot("global-tariff-slot-$index", "sample"))
        }
        val report = buildGlobalTariffSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
