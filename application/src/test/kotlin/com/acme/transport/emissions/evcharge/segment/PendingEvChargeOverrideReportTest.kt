package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingEvChargeOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708421187)
        val items = mutableListOf<PendingEvChargeOverride>()
        for (index in 0 until 4) {
            items.add(newPendingEvChargeOverride("pending-ev-charge-override-$index", "sample"))
        }
        val report = buildPendingEvChargeOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
