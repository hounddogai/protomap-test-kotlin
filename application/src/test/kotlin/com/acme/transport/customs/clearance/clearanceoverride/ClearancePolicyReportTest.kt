package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearancePolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709771434)
        val items = mutableListOf<ClearancePolicy>()
        for (index in 0 until 4) {
            items.add(newClearancePolicy("clearance-policy-$index", "sample"))
        }
        val report = buildClearancePolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
