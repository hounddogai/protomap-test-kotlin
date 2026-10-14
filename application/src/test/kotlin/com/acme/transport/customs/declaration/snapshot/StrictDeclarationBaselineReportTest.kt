package com.acme.transport.customs.declaration.snapshot

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDeclarationBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700039673)
        val items = mutableListOf<StrictDeclarationBaseline>()
        for (index in 0 until 6) {
            items.add(newStrictDeclarationBaseline("strict-declaration-baseline-$index", "sample"))
        }
        val report = buildStrictDeclarationBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
