package com.acme.transport.network.node.ledger

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryNodeVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702465621)
        val items = mutableListOf<PrimaryNodeVariance>()
        for (index in 0 until 6) {
            items.add(newPrimaryNodeVariance("primary-node-variance-$index", "sample"))
        }
        val report = buildPrimaryNodeVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
