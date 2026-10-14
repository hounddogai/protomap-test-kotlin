package com.acme.transport.fleetops.inspection.score

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedInspectionRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704674122)
        val items = mutableListOf<CappedInspectionRequest>()
        for (index in 0 until 2) {
            items.add(newCappedInspectionRequest("capped-inspection-request-$index", "sample"))
        }
        val report = buildCappedInspectionRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
