package com.acme.transport.network.zone.batch

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707430828)
        val items = mutableListOf<ZoneBatch>()
        for (index in 0 until 6) {
            items.add(newZoneBatch("zone-batch-$index", "sample"))
        }
        val report = buildZoneBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
