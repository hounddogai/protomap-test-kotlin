package com.acme.transport.safety.nearmiss.adjustment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701506630)
        val items = mutableListOf<NearMissAdjustment>()
        for (index in 0 until 5) {
            items.add(newNearMissAdjustment("near-miss-adjustment-$index", "sample"))
        }
        val report = buildNearMissAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
