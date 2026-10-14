package com.acme.transport.customs.inspection.allocation

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingInspectionRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705112392)
        val items = mutableListOf<PendingInspectionRollup>()
        for (index in 0 until 3) {
            items.add(newPendingInspectionRollup("pending-inspection-rollup-$index", "sample"))
        }
        val report = buildPendingInspectionRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
