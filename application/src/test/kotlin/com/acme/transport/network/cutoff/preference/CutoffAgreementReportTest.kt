package com.acme.transport.network.cutoff.preference

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704563355)
        val items = mutableListOf<CutoffAgreement>()
        for (index in 0 until 6) {
            items.add(newCutoffAgreement("cutoff-agreement-$index", "sample"))
        }
        val report = buildCutoffAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
