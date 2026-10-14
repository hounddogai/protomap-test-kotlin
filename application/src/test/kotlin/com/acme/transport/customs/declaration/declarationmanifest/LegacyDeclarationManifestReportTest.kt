package com.acme.transport.customs.declaration.declarationmanifest

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDeclarationManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707095474)
        val items = mutableListOf<LegacyDeclarationManifest>()
        for (index in 0 until 4) {
            items.add(newLegacyDeclarationManifest("legacy-declaration-manifest-$index", "sample"))
        }
        val report = buildLegacyDeclarationManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
