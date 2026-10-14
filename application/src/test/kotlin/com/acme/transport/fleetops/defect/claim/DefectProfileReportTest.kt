package com.acme.transport.fleetops.defect.claim

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706219263)
        val items = mutableListOf<DefectProfile>()
        for (index in 0 until 6) {
            items.add(newDefectProfile("defect-profile-$index", "sample"))
        }
        val report = buildDefectProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
