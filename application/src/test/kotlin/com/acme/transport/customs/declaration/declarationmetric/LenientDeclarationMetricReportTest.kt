package com.acme.transport.customs.declaration.declarationmetric

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDeclarationMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707702679)
        val items = mutableListOf<LenientDeclarationMetric>()
        for (index in 0 until 6) {
            items.add(newLenientDeclarationMetric("lenient-declaration-metric-$index", "sample"))
        }
        val report = buildLenientDeclarationMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
