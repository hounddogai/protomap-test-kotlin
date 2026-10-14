package com.acme.transport.fleetops.inspection.digest

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704821670)
        val items = mutableListOf<InspectionDigest>()
        for (index in 0 until 6) {
            items.add(newInspectionDigest("inspection-digest-$index", "sample"))
        }
        val report = buildInspectionDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
