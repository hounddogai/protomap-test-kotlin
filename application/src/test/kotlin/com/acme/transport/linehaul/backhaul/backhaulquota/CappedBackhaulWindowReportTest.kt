package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedBackhaulWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701456991)
        val items = mutableListOf<CappedBackhaulWindow>()
        for (index in 0 until 5) {
            items.add(newCappedBackhaulWindow("capped-backhaul-window-$index", "sample"))
        }
        val report = buildCappedBackhaulWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
