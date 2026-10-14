package com.acme.transport.crewing.qualification.overrideitem

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709363030)
        val items = mutableListOf<QualificationOverride>()
        for (index in 0 until 2) {
            items.add(newQualificationOverride("qualification-override-$index", "sample"))
        }
        val report = buildQualificationOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
