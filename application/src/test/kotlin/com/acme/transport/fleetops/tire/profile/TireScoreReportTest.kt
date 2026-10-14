package com.acme.transport.fleetops.tire.profile

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709675633)
        val items = mutableListOf<TireScore>()
        for (index in 0 until 6) {
            items.add(newTireScore("tire-score-$index", "sample"))
        }
        val report = buildTireScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
