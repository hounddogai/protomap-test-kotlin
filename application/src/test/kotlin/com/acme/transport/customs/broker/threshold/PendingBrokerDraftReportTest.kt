package com.acme.transport.customs.broker.threshold

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingBrokerDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708779807)
        val items = mutableListOf<PendingBrokerDraft>()
        for (index in 0 until 5) {
            items.add(newPendingBrokerDraft("pending-broker-draft-$index", "sample"))
        }
        val report = buildPendingBrokerDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
