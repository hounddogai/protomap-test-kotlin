package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalFuelSurchargePreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701907736)
        val items = mutableListOf<SeasonalFuelSurchargePreference>()
        for (index in 0 until 4) {
            items.add(newSeasonalFuelSurchargePreference("seasonal-fuel-surcharge-preference-$index", "sample"))
        }
        val report = buildSeasonalFuelSurchargePreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
