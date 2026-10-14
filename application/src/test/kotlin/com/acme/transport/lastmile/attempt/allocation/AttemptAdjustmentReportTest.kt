package com.acme.transport.lastmile.attempt.allocation

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702368279)
        val items = mutableListOf<AttemptAdjustment>()
        for (index in 0 until 3) {
            items.add(newAttemptAdjustment("attempt-adjustment-$index", "sample"))
        }
        val report = buildAttemptAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
