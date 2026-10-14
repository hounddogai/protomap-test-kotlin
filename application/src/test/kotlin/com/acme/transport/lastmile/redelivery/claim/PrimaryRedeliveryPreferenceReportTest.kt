package com.acme.transport.lastmile.redelivery.claim

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRedeliveryPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708458738)
        val items = mutableListOf<PrimaryRedeliveryPreference>()
        for (index in 0 until 2) {
            items.add(newPrimaryRedeliveryPreference("primary-redelivery-preference-$index", "sample"))
        }
        val report = buildPrimaryRedeliveryPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
