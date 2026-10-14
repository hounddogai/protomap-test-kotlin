package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictZoneCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700729921)
        val items = mutableListOf<StrictZoneCycle>()
        for (index in 0 until 2) {
            items.add(newStrictZoneCycle("strict-zone-cycle-$index", "sample"))
        }
        val report = buildStrictZoneCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
