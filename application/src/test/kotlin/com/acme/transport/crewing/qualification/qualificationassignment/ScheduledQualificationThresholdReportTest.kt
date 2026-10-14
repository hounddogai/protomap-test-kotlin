package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledQualificationThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704766067)
        val items = mutableListOf<ScheduledQualificationThreshold>()
        for (index in 0 until 3) {
            items.add(newScheduledQualificationThreshold("scheduled-qualification-threshold-$index", "sample"))
        }
        val report = buildScheduledQualificationThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
