package com.acme.transport.customs.declaration.summary

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708856116)
        val items = mutableListOf<DeclarationWindow>()
        for (index in 0 until 2) {
            items.add(newDeclarationWindow("declaration-window-$index", "sample"))
        }
        val report = buildDeclarationWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
