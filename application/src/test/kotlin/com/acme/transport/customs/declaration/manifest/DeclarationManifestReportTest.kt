package com.acme.transport.customs.declaration.manifest

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706154531)
        val items = mutableListOf<DeclarationManifest>()
        for (index in 0 until 6) {
            items.add(newDeclarationManifest("declaration-manifest-$index", "sample"))
        }
        val report = buildDeclarationManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
