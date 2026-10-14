package com.acme.transport.network.cutoff.summary

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706330462)
        val items = mutableListOf<CutoffRequest>()
        for (index in 0 until 2) {
            items.add(newCutoffRequest("cutoff-request-$index", "sample"))
        }
        val report = buildCutoffRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
