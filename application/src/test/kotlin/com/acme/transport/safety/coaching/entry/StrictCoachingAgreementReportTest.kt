package com.acme.transport.safety.coaching.entry

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCoachingAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703242420)
        val items = mutableListOf<StrictCoachingAgreement>()
        for (index in 0 until 6) {
            items.add(newStrictCoachingAgreement("strict-coaching-agreement-$index", "sample"))
        }
        val report = buildStrictCoachingAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
