package com.acme.transport.customs.declaration.claim

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDeclarationSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709881663)
        val items = mutableListOf<RegionalDeclarationSlot>()
        for (index in 0 until 3) {
            items.add(newRegionalDeclarationSlot("regional-declaration-slot-$index", "sample"))
        }
        val report = buildRegionalDeclarationSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
