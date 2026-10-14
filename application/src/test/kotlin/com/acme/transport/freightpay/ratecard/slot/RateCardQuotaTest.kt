package com.acme.transport.freightpay.ratecard.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardQuotaTest {
    @Test
    fun `starts in new`() {
        val item = newRateCardQuota("code-179", "label-542")
        assertEquals(RateCardQuotaStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRateCardQuota("code-179", "label-542")
        try {
            item.moveTo(RateCardQuotaStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardQuotaStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRateCardQuota("code-179", "label-542")
        item.status = RateCardQuotaStatus.FAILED
        assertFails("move") { item.moveTo(RateCardQuotaStatus.NEW) }
    }
}
