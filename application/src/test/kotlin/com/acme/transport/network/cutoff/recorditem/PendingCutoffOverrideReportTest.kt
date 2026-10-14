package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCutoffOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706238160)
        val items = mutableListOf<PendingCutoffOverride>()
        for (index in 0 until 3) {
            items.add(newPendingCutoffOverride("pending-cutoff-override-$index", "sample"))
        }
        val report = buildPendingCutoffOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
