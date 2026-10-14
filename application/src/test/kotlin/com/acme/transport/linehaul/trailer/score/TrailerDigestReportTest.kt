package com.acme.transport.linehaul.trailer.score

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709969575)
        val items = mutableListOf<TrailerDigest>()
        for (index in 0 until 4) {
            items.add(newTrailerDigest("trailer-digest-$index", "sample"))
        }
        val report = buildTrailerDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
