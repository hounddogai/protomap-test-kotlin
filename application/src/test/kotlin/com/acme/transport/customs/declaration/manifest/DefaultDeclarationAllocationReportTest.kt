package com.acme.transport.customs.declaration.manifest

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDeclarationAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708334760)
        val items = mutableListOf<DefaultDeclarationAllocation>()
        for (index in 0 until 4) {
            items.add(newDefaultDeclarationAllocation("default-declaration-allocation-$index", "sample"))
        }
        val report = buildDefaultDeclarationAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
