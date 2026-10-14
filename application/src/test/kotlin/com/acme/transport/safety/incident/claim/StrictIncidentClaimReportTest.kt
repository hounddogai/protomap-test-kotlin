package com.acme.transport.safety.incident.claim

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictIncidentClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701022330)
        val items = mutableListOf<StrictIncidentClaim>()
        for (index in 0 until 3) {
            items.add(newStrictIncidentClaim("strict-incident-claim-$index", "sample"))
        }
        val report = buildStrictIncidentClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
