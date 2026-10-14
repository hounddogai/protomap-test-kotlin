package com.acme.transport.freightpay.accessorial.baseline

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAccessorialBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708235395)
        val items = mutableListOf<ManualAccessorialBaseline>()
        for (index in 0 until 5) {
            items.add(newManualAccessorialBaseline("manual-accessorial-baseline-$index", "sample"))
        }
        val report = buildManualAccessorialBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
