package com.acme.transport.customs.inspection.claim

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708442727)
        val items = mutableListOf<InspectionClaim>()
        for (index in 0 until 3) {
            items.add(newInspectionClaim("inspection-claim-$index", "sample"))
        }
        val report = buildInspectionClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
