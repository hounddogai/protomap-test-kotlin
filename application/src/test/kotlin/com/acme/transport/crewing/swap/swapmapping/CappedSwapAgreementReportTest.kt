package com.acme.transport.crewing.swap.swapmapping

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedSwapAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705841366)
        val items = mutableListOf<CappedSwapAgreement>()
        for (index in 0 until 5) {
            items.add(newCappedSwapAgreement("capped-swap-agreement-$index", "sample"))
        }
        val report = buildCappedSwapAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
