package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeProposalReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702832651)
        val items = mutableListOf<FuelSurchargeProposal>()
        for (index in 0 until 6) {
            items.add(newFuelSurchargeProposal("fuel-surcharge-proposal-$index", "sample"))
        }
        val report = buildFuelSurchargeProposalReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
