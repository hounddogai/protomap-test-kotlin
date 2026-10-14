package com.acme.transport.freightpay.ratecard.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRateCardExceptionTest {
    @Test
    fun `starts in requested`() {
        val item = newStrictRateCardException("code-524", "label-352")
        assertEquals(StrictRateCardExceptionStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newStrictRateCardException("code-524", "label-352")
        try {
            item.moveTo(StrictRateCardExceptionStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRateCardExceptionStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newStrictRateCardException("code-524", "label-352")
        item.status = StrictRateCardExceptionStatus.EXPIRED
        assertFails("move") { item.moveTo(StrictRateCardExceptionStatus.REQUESTED) }
    }
}
