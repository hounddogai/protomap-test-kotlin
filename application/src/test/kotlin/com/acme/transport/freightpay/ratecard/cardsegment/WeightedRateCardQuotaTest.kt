package com.acme.transport.freightpay.ratecard.cardsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRateCardQuotaTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedRateCardQuota("code-757", "name-771")
        assertEquals(WeightedRateCardQuotaStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newWeightedRateCardQuota("code-757", "name-771")
        try {
            item.moveTo(WeightedRateCardQuotaStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRateCardQuotaStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedRateCardQuota("code-757", "name-771")
        item.status = WeightedRateCardQuotaStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedRateCardQuotaStatus.CREATED) }
    }
}
