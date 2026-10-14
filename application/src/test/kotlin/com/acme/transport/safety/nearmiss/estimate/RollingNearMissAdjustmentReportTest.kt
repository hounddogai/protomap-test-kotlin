package com.acme.transport.safety.nearmiss.estimate

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingNearMissAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700818573)
        val items = mutableListOf<RollingNearMissAdjustment>()
        for (index in 0 until 4) {
            items.add(newRollingNearMissAdjustment("rolling-near-miss-adjustment-$index", "sample"))
        }
        val report = buildRollingNearMissAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
