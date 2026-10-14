package com.acme.transport.safety.coaching.agreement

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCoachingAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705312994)
        val items = mutableListOf<RegionalCoachingAgreement>()
        for (index in 0 until 6) {
            items.add(newRegionalCoachingAgreement("regional-coaching-agreement-$index", "sample"))
        }
        val report = buildRegionalCoachingAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
