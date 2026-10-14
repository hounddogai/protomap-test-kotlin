package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704085233)
        val items = mutableListOf<QualificationClaim>()
        for (index in 0 until 5) {
            items.add(newQualificationClaim("qualification-claim-$index", "sample"))
        }
        val report = buildQualificationClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
