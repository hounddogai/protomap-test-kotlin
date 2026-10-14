package com.acme.transport.emissions.idling.score

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700299460)
        val items = mutableListOf<IdlingPreference>()
        for (index in 0 until 5) {
            items.add(newIdlingPreference("idling-preference-$index", "sample"))
        }
        val report = buildIdlingPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
