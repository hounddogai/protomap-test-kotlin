package com.acme.transport.network.zone.zonemapping

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientZoneAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708327055)
        val items = mutableListOf<LenientZoneAgreement>()
        for (index in 0 until 5) {
            items.add(newLenientZoneAgreement("lenient-zone-agreement-$index", "sample"))
        }
        val report = buildLenientZoneAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
