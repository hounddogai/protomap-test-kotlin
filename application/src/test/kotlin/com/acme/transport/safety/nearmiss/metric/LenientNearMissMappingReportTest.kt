package com.acme.transport.safety.nearmiss.metric

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientNearMissMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704580172)
        val items = mutableListOf<LenientNearMissMapping>()
        for (index in 0 until 4) {
            items.add(newLenientNearMissMapping("lenient-near-miss-mapping-$index", "sample"))
        }
        val report = buildLenientNearMissMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
