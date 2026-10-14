package com.acme.transport.freightpay.ratecard.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRateCardQuotaTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryRateCardQuota("code-589", "title-122")
        assertEquals(PrimaryRateCardQuotaStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPrimaryRateCardQuota("code-589", "title-122")
        try {
            item.moveTo(PrimaryRateCardQuotaStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRateCardQuotaStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryRateCardQuota("code-589", "title-122")
        item.status = PrimaryRateCardQuotaStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryRateCardQuotaStatus.REQUESTED) }
    }
}
