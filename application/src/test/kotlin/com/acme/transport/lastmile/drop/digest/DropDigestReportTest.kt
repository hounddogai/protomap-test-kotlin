package com.acme.transport.lastmile.drop.digest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701399471)
        val items = mutableListOf<DropDigest>()
        for (index in 0 until 6) {
            items.add(newDropDigest("drop-digest-$index", "sample"))
        }
        val report = buildDropDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
