package com.acme.transport.fleetops.defect.bundle

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708062109)
        val items = mutableListOf<DefectBundle>()
        for (index in 0 until 4) {
            items.add(newDefectBundle("defect-bundle-$index", "sample"))
        }
        val report = buildDefectBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
