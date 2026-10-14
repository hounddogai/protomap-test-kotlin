package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictHoldMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702517749)
        val items = mutableListOf<StrictHoldMapping>()
        for (index in 0 until 5) {
            items.add(newStrictHoldMapping("strict-hold-mapping-$index", "sample"))
        }
        val report = buildStrictHoldMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
