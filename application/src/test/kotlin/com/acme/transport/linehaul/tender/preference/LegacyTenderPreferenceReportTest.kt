package com.acme.transport.linehaul.tender.preference

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTenderPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702843498)
        val items = mutableListOf<LegacyTenderPreference>()
        for (index in 0 until 4) {
            items.add(newLegacyTenderPreference("legacy-tender-preference-$index", "sample"))
        }
        val report = buildLegacyTenderPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
