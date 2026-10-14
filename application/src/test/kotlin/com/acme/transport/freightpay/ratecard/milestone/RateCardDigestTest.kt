package com.acme.transport.freightpay.ratecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardDigestTest {
    @Test
    fun `starts in draft`() {
        val item = newRateCardDigest("code-623", "label-557")
        assertEquals(RateCardDigestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRateCardDigest("code-623", "label-557")
        try {
            item.moveTo(RateCardDigestStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardDigestStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRateCardDigest("code-623", "label-557")
        item.status = RateCardDigestStatus.CLOSED
        assertFails("move") { item.moveTo(RateCardDigestStatus.DRAFT) }
    }
}
