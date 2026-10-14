package com.acme.transport.freightpay.ratecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRateCardEntryTest {
    @Test
    fun `starts in open`() {
        val item = newLenientRateCardEntry("code-697", "name-298")
        assertEquals(LenientRateCardEntryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLenientRateCardEntry("code-697", "name-298")
        try {
            item.moveTo(LenientRateCardEntryStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRateCardEntryStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientRateCardEntry("code-697", "name-298")
        item.status = LenientRateCardEntryStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientRateCardEntryStatus.OPEN) }
    }
}
