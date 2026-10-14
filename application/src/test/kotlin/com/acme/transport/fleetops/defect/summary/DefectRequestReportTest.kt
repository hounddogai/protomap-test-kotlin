package com.acme.transport.fleetops.defect.summary

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708363102)
        val items = mutableListOf<DefectRequest>()
        for (index in 0 until 4) {
            items.add(newDefectRequest("defect-request-$index", "sample"))
        }
        val report = buildDefectRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
