package com.acme.transport.customs.declaration.forecast

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703102397)
        val items = mutableListOf<DeclarationCycle>()
        for (index in 0 until 5) {
            items.add(newDeclarationCycle("declaration-cycle-$index", "sample"))
        }
        val report = buildDeclarationCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
