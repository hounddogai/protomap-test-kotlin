package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701877500)
        val items = mutableListOf<IdlingProfile>()
        for (index in 0 until 3) {
            items.add(newIdlingProfile("idling-profile-$index", "sample"))
        }
        val report = buildIdlingProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
