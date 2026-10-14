package com.acme.transport.freightpay.ratecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRateCardPlanTest {
    @Test
    fun `starts in created`() {
        val item = newCappedRateCardPlan("code-231", "name-81")
        assertEquals(CappedRateCardPlanStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCappedRateCardPlan("code-231", "name-81")
        try {
            item.moveTo(CappedRateCardPlanStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRateCardPlanStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedRateCardPlan("code-231", "name-81")
        item.status = CappedRateCardPlanStatus.RETIRED
        assertFails("move") { item.moveTo(CappedRateCardPlanStatus.CREATED) }
    }
}
