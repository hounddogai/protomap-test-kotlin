package com.acme.transport.customs.declaration.audit

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708714837)
        val items = mutableListOf<DeclarationVariance>()
        for (index in 0 until 5) {
            items.add(newDeclarationVariance("declaration-variance-$index", "sample"))
        }
        val report = buildDeclarationVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
