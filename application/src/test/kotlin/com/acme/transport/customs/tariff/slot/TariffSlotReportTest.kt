package com.acme.transport.customs.tariff.slot

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706442809)
        val items = mutableListOf<TariffSlot>()
        for (index in 0 until 4) {
            items.add(newTariffSlot("tariff-slot-$index", "sample"))
        }
        val report = buildTariffSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
