package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704501939)
        val items = mutableListOf<BackhaulAgreement>()
        for (index in 0 until 6) {
            items.add(newBackhaulAgreement("backhaul-agreement-$index", "sample"))
        }
        val report = buildBackhaulAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
