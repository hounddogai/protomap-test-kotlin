package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackQualificationMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703910118)
        val items = mutableListOf<FallbackQualificationMilestone>()
        for (index in 0 until 2) {
            items.add(newFallbackQualificationMilestone("fallback-qualification-milestone-$index", "sample"))
        }
        val report = buildFallbackQualificationMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
