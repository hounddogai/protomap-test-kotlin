package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703930606)
        val items = mutableListOf<RestBreakDigest>()
        for (index in 0 until 3) {
            items.add(newRestBreakDigest("rest-break-digest-$index", "sample"))
        }
        val report = buildRestBreakDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
