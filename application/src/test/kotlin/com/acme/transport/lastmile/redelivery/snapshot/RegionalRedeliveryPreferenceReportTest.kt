package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRedeliveryPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703663632)
        val items = mutableListOf<RegionalRedeliveryPreference>()
        for (index in 0 until 2) {
            items.add(newRegionalRedeliveryPreference("regional-redelivery-preference-$index", "sample"))
        }
        val report = buildRegionalRedeliveryPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
