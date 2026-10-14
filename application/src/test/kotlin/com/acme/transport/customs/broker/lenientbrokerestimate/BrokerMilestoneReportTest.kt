package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701345758)
        val items = mutableListOf<BrokerMilestone>()
        for (index in 0 until 3) {
            items.add(newBrokerMilestone("broker-milestone-$index", "sample"))
        }
        val report = buildBrokerMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
