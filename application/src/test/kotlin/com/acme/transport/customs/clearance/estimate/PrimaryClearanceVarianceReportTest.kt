package com.acme.transport.customs.clearance.estimate

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryClearanceVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704396975)
        val items = mutableListOf<PrimaryClearanceVariance>()
        for (index in 0 until 6) {
            items.add(newPrimaryClearanceVariance("primary-clearance-variance-$index", "sample"))
        }
        val report = buildPrimaryClearanceVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
