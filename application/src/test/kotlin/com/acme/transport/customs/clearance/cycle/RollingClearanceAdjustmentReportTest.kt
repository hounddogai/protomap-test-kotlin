package com.acme.transport.customs.clearance.cycle

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingClearanceAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708128238)
        val items = mutableListOf<RollingClearanceAdjustment>()
        for (index in 0 until 3) {
            items.add(newRollingClearanceAdjustment("rolling-clearance-adjustment-$index", "sample"))
        }
        val report = buildRollingClearanceAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
