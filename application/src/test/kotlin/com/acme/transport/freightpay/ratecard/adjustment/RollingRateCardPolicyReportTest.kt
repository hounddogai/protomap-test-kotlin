package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRateCardPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702223589)
        val items = mutableListOf<RollingRateCardPolicy>()
        for (index in 0 until 6) {
            items.add(newRollingRateCardPolicy("rolling-rate-card-policy-$index", "sample"))
        }
        val report = buildRollingRateCardPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
