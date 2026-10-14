package com.acme.transport.freightpay.ratecard.cardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardExceptionTest {
    @Test
    fun `starts in open`() {
        val item = newRateCardException("code-210", "title-415")
        assertEquals(RateCardExceptionStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRateCardException("code-210", "title-415")
        try {
            item.moveTo(RateCardExceptionStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardExceptionStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRateCardException("code-210", "title-415")
        item.status = RateCardExceptionStatus.ARCHIVED
        assertFails("move") { item.moveTo(RateCardExceptionStatus.OPEN) }
    }
}
