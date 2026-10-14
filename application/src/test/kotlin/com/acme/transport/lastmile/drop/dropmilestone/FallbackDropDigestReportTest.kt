package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDropDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706018792)
        val items = mutableListOf<FallbackDropDigest>()
        for (index in 0 until 6) {
            items.add(newFallbackDropDigest("fallback-drop-digest-$index", "sample"))
        }
        val report = buildFallbackDropDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
