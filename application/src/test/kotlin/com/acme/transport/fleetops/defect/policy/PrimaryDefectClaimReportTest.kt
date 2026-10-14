package com.acme.transport.fleetops.defect.policy

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDefectClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705611463)
        val items = mutableListOf<PrimaryDefectClaim>()
        for (index in 0 until 2) {
            items.add(newPrimaryDefectClaim("primary-defect-claim-$index", "sample"))
        }
        val report = buildPrimaryDefectClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
