package com.acme.transport.freightpay.ratecard.cardrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveRateCardPlanTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveRateCardPlan("code-492", "name-475")
        assertEquals(AdaptiveRateCardPlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newAdaptiveRateCardPlan("code-492", "name-475")
        try {
            item.moveTo(AdaptiveRateCardPlanStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveRateCardPlanStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveRateCardPlan("code-492", "name-475")
        item.status = AdaptiveRateCardPlanStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveRateCardPlanStatus.IDLE) }
    }
}
