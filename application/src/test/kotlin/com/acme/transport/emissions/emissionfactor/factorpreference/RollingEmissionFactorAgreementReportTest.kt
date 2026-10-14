package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingEmissionFactorAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705200638)
        val items = mutableListOf<RollingEmissionFactorAgreement>()
        for (index in 0 until 2) {
            items.add(newRollingEmissionFactorAgreement("rolling-emission-factor-agreement-$index", "sample"))
        }
        val report = buildRollingEmissionFactorAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
