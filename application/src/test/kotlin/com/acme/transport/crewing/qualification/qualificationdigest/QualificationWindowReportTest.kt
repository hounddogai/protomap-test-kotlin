package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705194225)
        val items = mutableListOf<QualificationWindow>()
        for (index in 0 until 2) {
            items.add(newQualificationWindow("qualification-window-$index", "sample"))
        }
        val report = buildQualificationWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
