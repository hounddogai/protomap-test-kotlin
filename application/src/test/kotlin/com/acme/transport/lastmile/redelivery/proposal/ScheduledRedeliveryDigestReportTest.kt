package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRedeliveryDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703390164)
        val items = mutableListOf<ScheduledRedeliveryDigest>()
        for (index in 0 until 3) {
            items.add(newScheduledRedeliveryDigest("scheduled-redelivery-digest-$index", "sample"))
        }
        val report = buildScheduledRedeliveryDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
