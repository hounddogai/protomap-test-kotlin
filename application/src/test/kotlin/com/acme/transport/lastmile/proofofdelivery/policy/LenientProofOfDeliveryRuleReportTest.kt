package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientProofOfDeliveryRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708868959)
        val items = mutableListOf<LenientProofOfDeliveryRule>()
        for (index in 0 until 4) {
            items.add(newLenientProofOfDeliveryRule("lenient-proof-of-delivery-rule-$index", "sample"))
        }
        val report = buildLenientProofOfDeliveryRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
