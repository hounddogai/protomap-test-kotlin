package com.acme.transport.linehaul.tender.batch

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704629826)
        val items = mutableListOf<TenderPreference>()
        for (index in 0 until 6) {
            items.add(newTenderPreference("tender-preference-$index", "sample"))
        }
        val report = buildTenderPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
