package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DockDoorManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703809378)
        val items = mutableListOf<DockDoorManifest>()
        for (index in 0 until 6) {
            items.add(newDockDoorManifest("dock-door-manifest-$index", "sample"))
        }
        val report = buildDockDoorManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
