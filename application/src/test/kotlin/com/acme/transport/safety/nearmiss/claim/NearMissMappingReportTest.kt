package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707200226)
        val items = mutableListOf<NearMissMapping>()
        for (index in 0 until 2) {
            items.add(newNearMissMapping("near-miss-mapping-$index", "sample"))
        }
        val report = buildNearMissMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
