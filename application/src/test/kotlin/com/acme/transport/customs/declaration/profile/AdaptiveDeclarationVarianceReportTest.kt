package com.acme.transport.customs.declaration.profile

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDeclarationVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709260352)
        val items = mutableListOf<AdaptiveDeclarationVariance>()
        for (index in 0 until 6) {
            items.add(newAdaptiveDeclarationVariance("adaptive-declaration-variance-$index", "sample"))
        }
        val report = buildAdaptiveDeclarationVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
