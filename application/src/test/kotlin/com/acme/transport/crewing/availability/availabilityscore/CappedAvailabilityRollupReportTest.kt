package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAvailabilityRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703449091)
        val items = mutableListOf<CappedAvailabilityRollup>()
        for (index in 0 until 4) {
            items.add(newCappedAvailabilityRollup("capped-availability-rollup-$index", "sample"))
        }
        val report = buildCappedAvailabilityRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
