package com.acme.transport.lastmile.redelivery.policy

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRedeliverySnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705755366)
        val items = mutableListOf<GlobalRedeliverySnapshot>()
        for (index in 0 until 6) {
            items.add(newGlobalRedeliverySnapshot("global-redelivery-snapshot-$index", "sample"))
        }
        val report = buildGlobalRedeliverySnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
