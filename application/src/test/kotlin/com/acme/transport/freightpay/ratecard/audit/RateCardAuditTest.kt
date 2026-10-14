package com.acme.transport.freightpay.ratecard.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardAuditTest {
    @Test
    fun `starts in requested`() {
        val item = newRateCardAudit("code-372", "title-681")
        assertEquals(RateCardAuditStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRateCardAudit("code-372", "title-681")
        try {
            item.moveTo(RateCardAuditStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardAuditStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRateCardAudit("code-372", "title-681")
        item.status = RateCardAuditStatus.EXPIRED
        assertFails("move") { item.moveTo(RateCardAuditStatus.REQUESTED) }
    }
}
