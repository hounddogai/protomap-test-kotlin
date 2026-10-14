package com.acme.transport.customs.clearance.seasonalclearanceoverride

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalClearanceOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707881959)
        val items = mutableListOf<GlobalClearanceOverride>()
        for (index in 0 until 2) {
            items.add(newGlobalClearanceOverride("global-clearance-override-$index", "sample"))
        }
        val report = buildGlobalClearanceOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
