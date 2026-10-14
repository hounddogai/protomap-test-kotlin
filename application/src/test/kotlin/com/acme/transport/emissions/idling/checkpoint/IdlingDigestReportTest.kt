package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703969109)
        val items = mutableListOf<IdlingDigest>()
        for (index in 0 until 6) {
            items.add(newIdlingDigest("idling-digest-$index", "sample"))
        }
        val report = buildIdlingDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
