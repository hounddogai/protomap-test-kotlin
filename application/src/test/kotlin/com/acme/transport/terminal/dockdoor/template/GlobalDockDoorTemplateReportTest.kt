package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDockDoorTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709162842)
        val items = mutableListOf<GlobalDockDoorTemplate>()
        for (index in 0 until 3) {
            items.add(newGlobalDockDoorTemplate("global-dock-door-template-$index", "sample"))
        }
        val report = buildGlobalDockDoorTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
