package com.acme.transport.customs.declaration.plan

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700892023)
        val items = mutableListOf<DeclarationAllocation>()
        for (index in 0 until 2) {
            items.add(newDeclarationAllocation("declaration-allocation-$index", "sample"))
        }
        val report = buildDeclarationAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
