package com.acme.transport.freightpay.ratecard.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRateCardEntryTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictRateCardEntry("code-536", "title-372")
        assertEquals(StrictRateCardEntryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newStrictRateCardEntry("code-536", "title-372")
        try {
            item.moveTo(StrictRateCardEntryStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRateCardEntryStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictRateCardEntry("code-536", "title-372")
        item.status = StrictRateCardEntryStatus.CLOSED
        assertFails("move") { item.moveTo(StrictRateCardEntryStatus.DRAFT) }
    }
}
