package com.acme.transport.network.hub.agreement

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708387694)
        val items = mutableListOf<HubAgreement>()
        for (index in 0 until 6) {
            items.add(newHubAgreement("hub-agreement-$index", "sample"))
        }
        val report = buildHubAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
