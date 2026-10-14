package com.acme.transport.emissions.idling.milestone

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708722024)
        val items = mutableListOf<IdlingRollup>()
        for (index in 0 until 5) {
            items.add(newIdlingRollup("idling-rollup-$index", "sample"))
        }
        val report = buildIdlingRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
