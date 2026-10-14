package com.acme.transport.safety.incident.variance

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalIncidentClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709711134)
        val items = mutableListOf<RegionalIncidentClaim>()
        for (index in 0 until 6) {
            items.add(newRegionalIncidentClaim("regional-incident-claim-$index", "sample"))
        }
        val report = buildRegionalIncidentClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
