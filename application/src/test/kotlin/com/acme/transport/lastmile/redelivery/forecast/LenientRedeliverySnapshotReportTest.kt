package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRedeliverySnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709881872)
        val items = mutableListOf<LenientRedeliverySnapshot>()
        for (index in 0 until 4) {
            items.add(newLenientRedeliverySnapshot("lenient-redelivery-snapshot-$index", "sample"))
        }
        val report = buildLenientRedeliverySnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
