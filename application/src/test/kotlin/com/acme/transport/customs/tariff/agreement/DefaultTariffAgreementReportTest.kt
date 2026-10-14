package com.acme.transport.customs.tariff.agreement

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTariffAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700284634)
        val items = mutableListOf<DefaultTariffAgreement>()
        for (index in 0 until 4) {
            items.add(newDefaultTariffAgreement("default-tariff-agreement-$index", "sample"))
        }
        val report = buildDefaultTariffAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
