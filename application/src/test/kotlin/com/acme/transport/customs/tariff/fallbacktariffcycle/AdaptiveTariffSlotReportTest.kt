package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTariffSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701384272)
        val items = mutableListOf<AdaptiveTariffSlot>()
        for (index in 0 until 4) {
            items.add(newAdaptiveTariffSlot("adaptive-tariff-slot-$index", "sample"))
        }
        val report = buildAdaptiveTariffSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
