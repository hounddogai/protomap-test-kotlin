package com.acme.transport.freightpay.ratecard.cardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRateCardEntryTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalRateCardEntry("code-787", "name-33")
        assertEquals(GlobalRateCardEntryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newGlobalRateCardEntry("code-787", "name-33")
        try {
            item.moveTo(GlobalRateCardEntryStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRateCardEntryStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalRateCardEntry("code-787", "name-33")
        item.status = GlobalRateCardEntryStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalRateCardEntryStatus.OPEN) }
    }
}
