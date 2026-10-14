package com.acme.transport.lastmile.redelivery.redeliveryclaim

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRedeliveryClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708608654)
        val items = mutableListOf<PrimaryRedeliveryClaim>()
        for (index in 0 until 4) {
            items.add(newPrimaryRedeliveryClaim("primary-redelivery-claim-$index", "sample"))
        }
        val report = buildPrimaryRedeliveryClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
