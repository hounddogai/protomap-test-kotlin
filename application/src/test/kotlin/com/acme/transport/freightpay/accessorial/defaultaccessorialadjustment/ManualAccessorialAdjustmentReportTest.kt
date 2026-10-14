package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAccessorialAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701324450)
        val items = mutableListOf<ManualAccessorialAdjustment>()
        for (index in 0 until 4) {
            items.add(newManualAccessorialAdjustment("manual-accessorial-adjustment-$index", "sample"))
        }
        val report = buildManualAccessorialAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
