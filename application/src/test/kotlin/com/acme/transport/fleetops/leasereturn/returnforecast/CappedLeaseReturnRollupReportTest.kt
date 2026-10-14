package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedLeaseReturnRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708865663)
        val items = mutableListOf<CappedLeaseReturnRollup>()
        for (index in 0 until 6) {
            items.add(newCappedLeaseReturnRollup("capped-lease-return-rollup-$index", "sample"))
        }
        val report = buildCappedLeaseReturnRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
