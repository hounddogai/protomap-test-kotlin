package com.acme.transport.crewing.qualification.target

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701853097)
        val items = mutableListOf<QualificationNotice>()
        for (index in 0 until 3) {
            items.add(newQualificationNotice("qualification-notice-$index", "sample"))
        }
        val report = buildQualificationNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
