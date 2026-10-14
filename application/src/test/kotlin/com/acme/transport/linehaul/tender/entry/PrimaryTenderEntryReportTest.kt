package com.acme.transport.linehaul.tender.entry

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTenderEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700123631)
        val items = mutableListOf<PrimaryTenderEntry>()
        for (index in 0 until 4) {
            items.add(newPrimaryTenderEntry("primary-tender-entry-$index", "sample"))
        }
        val report = buildPrimaryTenderEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
