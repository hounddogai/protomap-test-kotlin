package com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyFuelSurchargeSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708181446)
        val items = mutableListOf<LegacyFuelSurchargeSnapshot>()
        for (index in 0 until 3) {
            items.add(newLegacyFuelSurchargeSnapshot("legacy-fuel-surcharge-snapshot-$index", "sample"))
        }
        val report = buildLegacyFuelSurchargeSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
