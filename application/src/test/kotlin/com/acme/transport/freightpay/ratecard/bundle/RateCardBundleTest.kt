package com.acme.transport.freightpay.ratecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardBundleTest {
    @Test
    fun `starts in proposed`() {
        val item = newRateCardBundle("code-278", "name-792")
        assertEquals(RateCardBundleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRateCardBundle("code-278", "name-792")
        try {
            item.moveTo(RateCardBundleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardBundleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRateCardBundle("code-278", "name-792")
        item.status = RateCardBundleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RateCardBundleStatus.PROPOSED) }
    }
}
