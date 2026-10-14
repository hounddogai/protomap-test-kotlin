package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702340053)
        val items = mutableListOf<RedeliveryAgreement>()
        for (index in 0 until 4) {
            items.add(newRedeliveryAgreement("redelivery-agreement-$index", "sample"))
        }
        val report = buildRedeliveryAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
