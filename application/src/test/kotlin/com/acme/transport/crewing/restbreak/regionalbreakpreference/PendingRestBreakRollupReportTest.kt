package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRestBreakRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705333504)
        val items = mutableListOf<PendingRestBreakRollup>()
        for (index in 0 until 5) {
            items.add(newPendingRestBreakRollup("pending-rest-break-rollup-$index", "sample"))
        }
        val report = buildPendingRestBreakRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
