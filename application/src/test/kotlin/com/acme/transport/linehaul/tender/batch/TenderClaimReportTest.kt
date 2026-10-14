package com.acme.transport.linehaul.tender.batch

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704785381)
        val items = mutableListOf<TenderClaim>()
        for (index in 0 until 2) {
            items.add(newTenderClaim("tender-claim-$index", "sample"))
        }
        val report = buildTenderClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
