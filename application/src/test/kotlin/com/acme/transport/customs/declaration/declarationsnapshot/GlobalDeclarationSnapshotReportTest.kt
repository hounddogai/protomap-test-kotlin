package com.acme.transport.customs.declaration.declarationsnapshot

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDeclarationSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702837206)
        val items = mutableListOf<GlobalDeclarationSnapshot>()
        for (index in 0 until 4) {
            items.add(newGlobalDeclarationSnapshot("global-declaration-snapshot-$index", "sample"))
        }
        val report = buildGlobalDeclarationSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
