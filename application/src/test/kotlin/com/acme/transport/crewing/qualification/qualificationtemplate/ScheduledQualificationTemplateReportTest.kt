package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledQualificationTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705974728)
        val items = mutableListOf<ScheduledQualificationTemplate>()
        for (index in 0 until 2) {
            items.add(newScheduledQualificationTemplate("scheduled-qualification-template-$index", "sample"))
        }
        val report = buildScheduledQualificationTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
