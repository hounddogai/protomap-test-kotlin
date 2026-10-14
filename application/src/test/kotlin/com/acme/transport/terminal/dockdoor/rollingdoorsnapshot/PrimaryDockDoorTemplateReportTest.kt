package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDockDoorTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706175679)
        val items = mutableListOf<PrimaryDockDoorTemplate>()
        for (index in 0 until 3) {
            items.add(newPrimaryDockDoorTemplate("primary-dock-door-template-$index", "sample"))
        }
        val report = buildPrimaryDockDoorTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
