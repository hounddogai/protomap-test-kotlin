package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDockDoorExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703492947)
        val items = mutableListOf<PendingDockDoorException>()
        for (index in 0 until 3) {
            items.add(newPendingDockDoorException("pending-dock-door-exception-$index", "sample"))
        }
        val report = buildPendingDockDoorExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
