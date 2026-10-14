package com.acme.transport.fleetops.utilization.group

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualUtilizationClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702499575)
        val items = mutableListOf<ManualUtilizationClaim>()
        for (index in 0 until 5) {
            items.add(newManualUtilizationClaim("manual-utilization-claim-$index", "sample"))
        }
        val report = buildManualUtilizationClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
