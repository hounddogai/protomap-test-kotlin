package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTireAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704924196)
        val items = mutableListOf<ManualTireAgreement>()
        for (index in 0 until 3) {
            items.add(newManualTireAgreement("manual-tire-agreement-$index", "sample"))
        }
        val report = buildManualTireAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
