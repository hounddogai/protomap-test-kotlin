package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706076999)
        val items = mutableListOf<RedeliveryPreference>()
        for (index in 0 until 2) {
            items.add(newRedeliveryPreference("redelivery-preference-$index", "sample"))
        }
        val report = buildRedeliveryPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
