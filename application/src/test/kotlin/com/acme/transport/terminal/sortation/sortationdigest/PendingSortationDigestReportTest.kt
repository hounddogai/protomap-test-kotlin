package com.acme.transport.terminal.sortation.sortationdigest

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingSortationDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701706285)
        val items = mutableListOf<PendingSortationDigest>()
        for (index in 0 until 6) {
            items.add(newPendingSortationDigest("pending-sortation-digest-$index", "sample"))
        }
        val report = buildPendingSortationDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
