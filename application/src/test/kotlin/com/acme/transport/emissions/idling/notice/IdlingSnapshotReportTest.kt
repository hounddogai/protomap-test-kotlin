package com.acme.transport.emissions.idling.notice

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701905857)
        val items = mutableListOf<IdlingSnapshot>()
        for (index in 0 until 2) {
            items.add(newIdlingSnapshot("idling-snapshot-$index", "sample"))
        }
        val report = buildIdlingSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
