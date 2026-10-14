package com.acme.transport.freightpay.ratecard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRateCardWindowTest {
    @Test
    fun `starts in open`() {
        val item = newCappedRateCardWindow("code-86", "title-71")
        assertEquals(CappedRateCardWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCappedRateCardWindow("code-86", "title-71")
        try {
            item.moveTo(CappedRateCardWindowStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRateCardWindowStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedRateCardWindow("code-86", "title-71")
        item.status = CappedRateCardWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedRateCardWindowStatus.OPEN) }
    }
}
