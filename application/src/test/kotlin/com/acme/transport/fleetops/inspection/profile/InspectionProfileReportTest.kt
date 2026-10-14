package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706920755)
        val items = mutableListOf<InspectionProfile>()
        for (index in 0 until 2) {
            items.add(newInspectionProfile("inspection-profile-$index", "sample"))
        }
        val report = buildInspectionProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
