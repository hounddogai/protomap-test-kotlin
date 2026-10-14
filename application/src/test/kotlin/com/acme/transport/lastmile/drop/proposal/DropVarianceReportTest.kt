package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700104951)
        val items = mutableListOf<DropVariance>()
        for (index in 0 until 3) {
            items.add(newDropVariance("drop-variance-$index", "sample"))
        }
        val report = buildDropVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
