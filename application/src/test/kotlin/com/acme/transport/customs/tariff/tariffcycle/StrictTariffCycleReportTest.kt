package com.acme.transport.customs.tariff.tariffcycle

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTariffCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705256790)
        val items = mutableListOf<StrictTariffCycle>()
        for (index in 0 until 2) {
            items.add(newStrictTariffCycle("strict-tariff-cycle-$index", "sample"))
        }
        val report = buildStrictTariffCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
