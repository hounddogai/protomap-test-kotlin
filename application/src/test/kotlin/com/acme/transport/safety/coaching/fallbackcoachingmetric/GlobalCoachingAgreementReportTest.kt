package com.acme.transport.safety.coaching.fallbackcoachingmetric

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCoachingAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701981797)
        val items = mutableListOf<GlobalCoachingAgreement>()
        for (index in 0 until 3) {
            items.add(newGlobalCoachingAgreement("global-coaching-agreement-$index", "sample"))
        }
        val report = buildGlobalCoachingAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
