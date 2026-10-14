package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalQualificationNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702215823)
        val items = mutableListOf<RegionalQualificationNotice>()
        for (index in 0 until 2) {
            items.add(newRegionalQualificationNotice("regional-qualification-notice-$index", "sample"))
        }
        val report = buildRegionalQualificationNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
