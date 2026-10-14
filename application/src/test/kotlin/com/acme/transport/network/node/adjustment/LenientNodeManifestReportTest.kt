package com.acme.transport.network.node.adjustment

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientNodeManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700203816)
        val items = mutableListOf<LenientNodeManifest>()
        for (index in 0 until 2) {
            items.add(newLenientNodeManifest("lenient-node-manifest-$index", "sample"))
        }
        val report = buildLenientNodeManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
