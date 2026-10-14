package com.acme.transport.crewing.qualification.request

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalQualificationWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700458838)
        val items = mutableListOf<RegionalQualificationWindow>()
        for (index in 0 until 5) {
            items.add(newRegionalQualificationWindow("regional-qualification-window-$index", "sample"))
        }
        val report = buildRegionalQualificationWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
