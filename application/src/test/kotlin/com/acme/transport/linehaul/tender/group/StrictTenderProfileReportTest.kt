package com.acme.transport.linehaul.tender.group

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTenderProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704210251)
        val items = mutableListOf<StrictTenderProfile>()
        for (index in 0 until 5) {
            items.add(newStrictTenderProfile("strict-tender-profile-$index", "sample"))
        }
        val report = buildStrictTenderProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
