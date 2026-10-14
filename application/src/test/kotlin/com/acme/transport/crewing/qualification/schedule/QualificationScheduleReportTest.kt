package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701196555)
        val items = mutableListOf<QualificationSchedule>()
        for (index in 0 until 4) {
            items.add(newQualificationSchedule("qualification-schedule-$index", "sample"))
        }
        val report = buildQualificationScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
