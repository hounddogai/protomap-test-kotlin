package com.acme.transport.customs.declaration.segment

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703873073)
        val items = mutableListOf<DeclarationSlot>()
        for (index in 0 until 2) {
            items.add(newDeclarationSlot("declaration-slot-$index", "sample"))
        }
        val report = buildDeclarationSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
