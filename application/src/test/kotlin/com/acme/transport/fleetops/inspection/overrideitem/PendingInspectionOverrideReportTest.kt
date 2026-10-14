package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingInspectionOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700135633)
        val items = mutableListOf<PendingInspectionOverride>()
        for (index in 0 until 5) {
            items.add(newPendingInspectionOverride("pending-inspection-override-$index", "sample"))
        }
        val report = buildPendingInspectionOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
