package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRateCardDraftTest {
    @Test
    fun `starts in planned`() {
        val item = newPrimaryRateCardDraft("code-817", "name-863")
        assertEquals(PrimaryRateCardDraftStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newPrimaryRateCardDraft("code-817", "name-863")
        try {
            item.moveTo(PrimaryRateCardDraftStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRateCardDraftStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPrimaryRateCardDraft("code-817", "name-863")
        item.status = PrimaryRateCardDraftStatus.CANCELLED
        assertFails("move") { item.moveTo(PrimaryRateCardDraftStatus.PLANNED) }
    }
}
