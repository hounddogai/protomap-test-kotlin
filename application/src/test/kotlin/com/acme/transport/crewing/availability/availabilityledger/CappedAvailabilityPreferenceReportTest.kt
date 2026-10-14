package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAvailabilityPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701172812)
        val items = mutableListOf<CappedAvailabilityPreference>()
        for (index in 0 until 2) {
            items.add(newCappedAvailabilityPreference("capped-availability-preference-$index", "sample"))
        }
        val report = buildCappedAvailabilityPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
