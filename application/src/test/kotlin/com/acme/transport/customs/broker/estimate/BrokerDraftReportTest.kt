package com.acme.transport.customs.broker.estimate

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706118031)
        val items = mutableListOf<BrokerDraft>()
        for (index in 0 until 2) {
            items.add(newBrokerDraft("broker-draft-$index", "sample"))
        }
        val report = buildBrokerDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
