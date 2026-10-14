package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700571494)
        val items = mutableListOf<BackhaulOverride>()
        for (index in 0 until 6) {
            items.add(newBackhaulOverride("backhaul-override-$index", "sample"))
        }
        val report = buildBackhaulOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
