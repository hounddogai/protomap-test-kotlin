package com.acme.transport.freightpay.ratecard.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newRateCardEntry("code-765", "name-179")
        assertEquals(RateCardEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRateCardEntry("code-765", "name-179")
        try {
            item.moveTo(RateCardEntryStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardEntryStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRateCardEntry("code-765", "name-179")
        item.status = RateCardEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(RateCardEntryStatus.REQUESTED) }
    }
}
