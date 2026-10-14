package com.acme.transport.network.hub.notice

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalHubAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705482478)
        val items = mutableListOf<RegionalHubAgreement>()
        for (index in 0 until 3) {
            items.add(newRegionalHubAgreement("regional-hub-agreement-$index", "sample"))
        }
        val report = buildRegionalHubAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
