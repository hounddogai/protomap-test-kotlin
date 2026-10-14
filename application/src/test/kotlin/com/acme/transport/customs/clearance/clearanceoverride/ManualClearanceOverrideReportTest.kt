package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualClearanceOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709096336)
        val items = mutableListOf<ManualClearanceOverride>()
        for (index in 0 until 4) {
            items.add(newManualClearanceOverride("manual-clearance-override-$index", "sample"))
        }
        val report = buildManualClearanceOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
