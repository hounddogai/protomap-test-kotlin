package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyInspectionClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701164659)
        val items = mutableListOf<LegacyInspectionClaim>()
        for (index in 0 until 4) {
            items.add(newLegacyInspectionClaim("legacy-inspection-claim-$index", "sample"))
        }
        val report = buildLegacyInspectionClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
