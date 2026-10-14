package com.acme.transport.freightpay.ratecard.cardbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRateCardBundleTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalRateCardBundle("code-186", "title-322")
        assertEquals(GlobalRateCardBundleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newGlobalRateCardBundle("code-186", "title-322")
        try {
            item.moveTo(GlobalRateCardBundleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRateCardBundleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalRateCardBundle("code-186", "title-322")
        item.status = GlobalRateCardBundleStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalRateCardBundleStatus.DRAFT) }
    }
}
