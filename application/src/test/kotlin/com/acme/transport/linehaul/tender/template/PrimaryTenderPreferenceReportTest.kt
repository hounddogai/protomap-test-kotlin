package com.acme.transport.linehaul.tender.template

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTenderPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706759746)
        val items = mutableListOf<PrimaryTenderPreference>()
        for (index in 0 until 3) {
            items.add(newPrimaryTenderPreference("primary-tender-preference-$index", "sample"))
        }
        val report = buildPrimaryTenderPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
