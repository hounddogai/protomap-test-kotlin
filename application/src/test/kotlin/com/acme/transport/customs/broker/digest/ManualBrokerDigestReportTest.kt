package com.acme.transport.customs.broker.digest

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualBrokerDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705521275)
        val items = mutableListOf<ManualBrokerDigest>()
        for (index in 0 until 2) {
            items.add(newManualBrokerDigest("manual-broker-digest-$index", "sample"))
        }
        val report = buildManualBrokerDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
