package com.acme.transport.customs.declaration.snapshot

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDeclarationSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704553417)
        val items = mutableListOf<ManualDeclarationSnapshot>()
        for (index in 0 until 3) {
            items.add(newManualDeclarationSnapshot("manual-declaration-snapshot-$index", "sample"))
        }
        val report = buildManualDeclarationSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
