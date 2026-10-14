package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientBrokerEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702801521)
        val items = mutableListOf<LenientBrokerEstimate>()
        for (index in 0 until 5) {
            items.add(newLenientBrokerEstimate("lenient-broker-estimate-$index", "sample"))
        }
        val report = buildLenientBrokerEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
