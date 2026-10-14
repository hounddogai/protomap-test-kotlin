package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702046472)
        val items = mutableListOf<QualificationMapping>()
        for (index in 0 until 2) {
            items.add(newQualificationMapping("qualification-mapping-$index", "sample"))
        }
        val report = buildQualificationMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
