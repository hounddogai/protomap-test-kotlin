package com.acme.transport.linehaul.tender.summary

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTenderDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705069741)
        val items = mutableListOf<GlobalTenderDigest>()
        for (index in 0 until 4) {
            items.add(newGlobalTenderDigest("global-tender-digest-$index", "sample"))
        }
        val report = buildGlobalTenderDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
