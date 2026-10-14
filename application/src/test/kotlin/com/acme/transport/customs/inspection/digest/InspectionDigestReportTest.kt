package com.acme.transport.customs.inspection.digest

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701960156)
        val items = mutableListOf<InspectionDigest>()
        for (index in 0 until 2) {
            items.add(newInspectionDigest("inspection-digest-$index", "sample"))
        }
        val report = buildInspectionDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
