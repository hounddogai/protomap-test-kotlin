package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700123541)
        val items = mutableListOf<RedeliveryDraft>()
        for (index in 0 until 2) {
            items.add(newRedeliveryDraft("redelivery-draft-$index", "sample"))
        }
        val report = buildRedeliveryDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
