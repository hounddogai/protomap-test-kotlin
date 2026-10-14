package com.acme.transport.freightpay.ratecard.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardDraftTest {
    @Test
    fun `starts in planned`() {
        val item = newRateCardDraft("code-49", "label-274")
        assertEquals(RateCardDraftStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRateCardDraft("code-49", "label-274")
        try {
            item.moveTo(RateCardDraftStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardDraftStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRateCardDraft("code-49", "label-274")
        item.status = RateCardDraftStatus.CANCELLED
        assertFails("move") { item.moveTo(RateCardDraftStatus.PLANNED) }
    }
}
