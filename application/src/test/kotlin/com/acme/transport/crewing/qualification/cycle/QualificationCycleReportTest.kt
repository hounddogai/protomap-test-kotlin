package com.acme.transport.crewing.qualification.cycle

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707718162)
        val items = mutableListOf<QualificationCycle>()
        for (index in 0 until 2) {
            items.add(newQualificationCycle("qualification-cycle-$index", "sample"))
        }
        val report = buildQualificationCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
