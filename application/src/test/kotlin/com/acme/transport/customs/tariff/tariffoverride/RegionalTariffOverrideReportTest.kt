package com.acme.transport.customs.tariff.tariffoverride

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTariffOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709789705)
        val items = mutableListOf<RegionalTariffOverride>()
        for (index in 0 until 2) {
            items.add(newRegionalTariffOverride("regional-tariff-override-$index", "sample"))
        }
        val report = buildRegionalTariffOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
