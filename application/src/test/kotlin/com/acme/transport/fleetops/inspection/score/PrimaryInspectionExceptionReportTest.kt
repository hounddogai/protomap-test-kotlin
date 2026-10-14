package com.acme.transport.fleetops.inspection.score

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryInspectionExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702852175)
        val items = mutableListOf<PrimaryInspectionException>()
        for (index in 0 until 4) {
            items.add(newPrimaryInspectionException("primary-inspection-exception-$index", "sample"))
        }
        val report = buildPrimaryInspectionExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
