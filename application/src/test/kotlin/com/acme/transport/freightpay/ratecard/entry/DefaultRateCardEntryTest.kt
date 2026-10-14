package com.acme.transport.freightpay.ratecard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRateCardEntryTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultRateCardEntry("code-546", "title-386")
        assertEquals(DefaultRateCardEntryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newDefaultRateCardEntry("code-546", "title-386")
        try {
            item.moveTo(DefaultRateCardEntryStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRateCardEntryStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultRateCardEntry("code-546", "title-386")
        item.status = DefaultRateCardEntryStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultRateCardEntryStatus.OPEN) }
    }
}
