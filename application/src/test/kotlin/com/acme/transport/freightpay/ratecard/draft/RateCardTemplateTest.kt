package com.acme.transport.freightpay.ratecard.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardTemplateTest {
    @Test
    fun `starts in new`() {
        val item = newRateCardTemplate("code-536", "label-30")
        assertEquals(RateCardTemplateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRateCardTemplate("code-536", "label-30")
        try {
            item.moveTo(RateCardTemplateStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardTemplateStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRateCardTemplate("code-536", "label-30")
        item.status = RateCardTemplateStatus.FAILED
        assertFails("move") { item.moveTo(RateCardTemplateStatus.NEW) }
    }
}
