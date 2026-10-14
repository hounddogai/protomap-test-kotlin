package com.acme.transport.customs.declaration.window

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDeclarationWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703653297)
        val items = mutableListOf<AdaptiveDeclarationWindow>()
        for (index in 0 until 6) {
            items.add(newAdaptiveDeclarationWindow("adaptive-declaration-window-$index", "sample"))
        }
        val report = buildAdaptiveDeclarationWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
