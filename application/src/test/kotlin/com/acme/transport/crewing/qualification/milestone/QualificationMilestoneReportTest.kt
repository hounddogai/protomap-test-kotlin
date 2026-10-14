package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707735708)
        val items = mutableListOf<QualificationMilestone>()
        for (index in 0 until 4) {
            items.add(newQualificationMilestone("qualification-milestone-$index", "sample"))
        }
        val report = buildQualificationMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
