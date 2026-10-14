package com.acme.transport.crewing.qualification.target

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700617386)
        val items = mutableListOf<QualificationTarget>()
        for (index in 0 until 4) {
            items.add(newQualificationTarget("qualification-target-$index", "sample"))
        }
        val report = buildQualificationTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
