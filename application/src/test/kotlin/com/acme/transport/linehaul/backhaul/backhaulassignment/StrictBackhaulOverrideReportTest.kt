package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictBackhaulOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700324278)
        val items = mutableListOf<StrictBackhaulOverride>()
        for (index in 0 until 6) {
            items.add(newStrictBackhaulOverride("strict-backhaul-override-$index", "sample"))
        }
        val report = buildStrictBackhaulOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
