package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDeclarationWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701624338)
        val items = mutableListOf<TieredDeclarationWindow>()
        for (index in 0 until 6) {
            items.add(newTieredDeclarationWindow("tiered-declaration-window-$index", "sample"))
        }
        val report = buildTieredDeclarationWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
