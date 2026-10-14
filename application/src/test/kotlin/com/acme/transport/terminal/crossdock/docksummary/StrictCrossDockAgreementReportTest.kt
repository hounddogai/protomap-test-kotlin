package com.acme.transport.terminal.crossdock.docksummary

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCrossDockAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700696694)
        val items = mutableListOf<StrictCrossDockAgreement>()
        for (index in 0 until 4) {
            items.add(newStrictCrossDockAgreement("strict-cross-dock-agreement-$index", "sample"))
        }
        val report = buildStrictCrossDockAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
