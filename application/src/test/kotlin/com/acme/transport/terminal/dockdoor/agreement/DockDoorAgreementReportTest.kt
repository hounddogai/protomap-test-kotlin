package com.acme.transport.terminal.dockdoor.agreement

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701947215)
        val items = mutableListOf<DockDoorAgreement>()
        for (index in 0 until 6) {
            items.add(newDockDoorAgreement("dock-door-agreement-$index", "sample"))
        }
        val report = buildDockDoorAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
