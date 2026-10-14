package com.acme.transport.freightpay.ratecard.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRateCardAuditTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultRateCardAudit("code-10", "name-92")
        assertEquals(DefaultRateCardAuditStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDefaultRateCardAudit("code-10", "name-92")
        try {
            item.moveTo(DefaultRateCardAuditStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRateCardAuditStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultRateCardAudit("code-10", "name-92")
        item.status = DefaultRateCardAuditStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultRateCardAuditStatus.DRAFT) }
    }
}
